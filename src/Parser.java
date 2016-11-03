import sun.rmi.runtime.Log;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by MikiraSora on 2016/10/31.
 */
public class Parser {
    /*片元 定义区*/
    static class Unit{
        enum UnitType{
            Statement,/*
            ConditionBranch,
            LoopBranch,*/
            Symbol,
            Unknown
        }

        @Override
        public String toString() {
            return Integer.toString(line);
        }

        //protected UnitType unit_type=UnitType.Unknown;
        public UnitType GetType(){return UnitType.Unknown;}

        protected int line=-1;
        public int GetLine(){return line;}

        Unit(){}
        Unit(int line){this.line=line;}
    }

    static class Statement extends Unit{

        enum StatementType{
            Function,
            Call,
            Return,
            Goto,
            Set,
            Unknown
        }

        @Override
        public String toString() {
            return String.format("%s - %s : %s",super.toString(),this.GetStatementType().toString(),statement_context);
        }

        //String current_paramster="";

        public abstract class Action{
            void onExecute(Object object){}
        }

        String statement_context=null;
        private Statement(){}
        Statement(int line,String context){
            super(line);
            statement_context=context;
        }

        @Override
        public UnitType GetType() {
            return UnitType.Statement;
        }

        public StatementType GetStatementType(){return StatementType.Unknown;}

        void Execute()throws Exception{throw new Exception("this statement haven't be executed.");}

        static class Function extends Statement{
            int end_line=-1;

            private Function(){super();}
            Function(int line,String body)throws Exception{
                super(line,body);
                Matcher result = FunctionFormatRegex.matcher(statement_context);
                result.find();
                if (result.groupCount() != 2)
                    throw new Exception("Cant parse function :"+statement_context);
                //Log.ExceptionError( new Exception("Cannot parse function ：" + expression));
                function_name = result.group(1);
                request=new Calculator.Function.ParameterRequest(result.group(2));
            }

            private static Pattern FunctionFormatRegex = Pattern.compile("([a-zA-Z]\\w*)\\((.*)\\)");
            String function_name=null;
            public Calculator.Function.ParameterRequest request=null;

            String GetFunctionName(){
                return function_name;
            }

            int GetParameterRequestCount(){return request.GetParamterRequestCount();}

            @Override
            public StatementType GetStatementType() {
                return StatementType.Function;
            }

            static enum FunctionType{
                Begin,End,Unknown
            }

            @Override
            public String toString() {
                return String.format("%s - %s",super.toString(),this.GetFunctionType().toString());
            }

            public FunctionType GetFunctionType(){return FunctionType.Unknown;}

            static class FunctionBody extends Function{
                private FunctionBody(){}
                FunctionBody(int line,String body)throws Exception{super(line,body);}

                @Override
                public FunctionType GetFunctionType() {
                    return FunctionType.Begin;
                }
            }

            static class EndFcuntion extends Function{
                private EndFcuntion(){}
                EndFcuntion(int line)throws Exception{super(line,"");}

                @Override
                public FunctionType GetFunctionType() {
                    return FunctionType.End;
                }
            }
        }

        static class Call extends Statement{
            private Call(){}
            Call(int line,String name){super(line,name);}

            @Override
            public StatementType GetStatementType() {
                return StatementType.Call;
            }
        }

        static class Set extends Statement{
            String variable_name=null,variable_value=null;
            private Set(){}
            Set(int line,String name) {
                super(line, name);
                char c;
                for (int position = 0; position < statement_context.length() - 1; position++) {
                    c = statement_context.charAt(position);
                    if (c == '=') {
                        variable_value = statement_context.substring(position + 1);
                        break;
                    } else {
                        variable_name += c;
                    }
                }
            }

            @Override
            public StatementType GetStatementType() {
                return StatementType.Set;
            }
        }

        static class Goto extends Statement{
            private Goto(){}
            Goto(int line,String name){super(line,name);}

            @Override
            public StatementType GetStatementType() {
                return StatementType.Goto;
            }
        }

        static class Return extends Statement{
            private Return(){}
            Return(int line,String name){super(line,name);}

            @Override
            public StatementType GetStatementType() {
                return StatementType.Return;
            }
        }

    }

    static class Symbol extends Unit{
        enum SymbolType{
            ConditionBranch,
            LoopBranch,
            Label,
            Unknown
        }

        @Override
        public String toString() {
            return String.format("%s - %s",super.toString(),this.GetSymbolType().toString());
        }

        //protected SymbolType symbol_type=SymbolType.Unknown;
        public SymbolType GetSymbolType(){return SymbolType.Unknown;}

        @Override
        public UnitType GetType() {
            return UnitType.Symbol;
        }

        private Symbol(){}
        Symbol(int line){super(line);}

        /**Condition Defination*/
        public static class Condition extends Symbol{
            int end_line=-1,else_line=-1;
            If reference_if=null;

            private Condition(){}
            Condition(int line){super(line);}

            @Override
            public String toString() {
                return String.format("%s : %s",super.toString(),GetConditionType().toString());
            }

            @Override
            public SymbolType GetSymbolType() {
                return SymbolType.ConditionBranch;
            }

            enum ConditionType{
                If,Then,Else,EndIf,Unknown
            }

            public ConditionType GetConditionType(){return ConditionType.Unknown;}
            /**Condition sub-classes*/
            static class If extends Condition{
                String condition=null;
                private If(){}
                If(int line,String condition){super(line);this.condition=condition;}

                @Override
                public ConditionType GetConditionType() {
                    return ConditionType.If;
                }
            }

            static class Then extends Condition{
                private Then(){}
                Then(int line){super(line);}

                @Override
                public ConditionType GetConditionType() {
                    return ConditionType.Then;
                }
            }

            static class Else extends Condition{
                private Else(){}
                Else(int line){super(line);}

                @Override
                public ConditionType GetConditionType() {
                    return ConditionType.Else;
                }
            }

            static class EndIf extends Condition{
                private EndIf(){}
                EndIf(int line){super(line);}

                @Override
                public ConditionType GetConditionType() {
                    return ConditionType.EndIf;
                }
            }
        }

        /**Loop Defination*/
        static class Loop extends Symbol{
            LoopBegin reference_loop=null;
            int end_line=-1;
            private Loop(){}
            Loop(int line){super(line);}

            @Override
            public SymbolType GetSymbolType() {
                return SymbolType.LoopBranch;
            }

            enum LoopType{
               LoopBegin,Continue,Break,Endloop,Unknown
            }

            @Override
            public String toString() {
                return String.format("%s : %s",super.toString(),GetLoopType().toString());
            }

            public LoopType GetLoopType(){return LoopType.Unknown;}

            static class LoopBegin extends Loop{
                //int end_line=-1;

                private LoopBegin(){}
                LoopBegin(int line){super(line);}

                @Override
                public LoopType GetLoopType() {
                    return LoopType.LoopBegin;
                }
            }

            static class Continue extends Loop{
                private Continue(){}
                Continue(int line){super(line);}

                @Override
                public LoopType GetLoopType() {
                    return LoopType.Continue;
                }
            }

            static class Break extends Loop{
                private Break(){}
                Break(int line){super(line);}

                @Override
                public LoopType GetLoopType() {
                    return LoopType.Break;
                }
            }

            static class Endloop extends Loop{
                private Endloop(){}
                Endloop(int line){super(line);}

                @Override
                public LoopType GetLoopType() {
                    return LoopType.Endloop;
                }
            }
        }

        /**Label handler Defination*/
        class Label extends Symbol{
            private Label(){}
            Label(int line){super(line);}

            @Override
            public SymbolType GetSymbolType() {
                return SymbolType.Label;
            }
        }
    }

    /*******/
    /**缓存区**/
    HashMap<Integer,Unit> StatementLine=new HashMap<>();
    ArrayList<String> IncludeFileName=new ArrayList<>();
    /***Cache****/
    HashMap<String,Integer> LabelPositionCache=new HashMap<>();
    HashMap<String, Statement.Function> FunctionTable=new HashMap<>();

    /***属性***/
    public HashMap<String,String> propherty=new HashMap<>();
    public void Set(String key,String value){propherty.put(key,value);}
    public String Get(String key){return propherty.get(key);}
    /*
        #include "C:\stdmath.ml"

        #package "NormalMath"
        #version 4.5

        function max(a,b)
            if(a>b)
            then
                return a
            else
                return b
            endif
        endfuntion

        function min(a,b)
            if(max(a,b)==a)
            then
               return a
            else
               return b
            endif
        endfunction
    */
    void PushStatement(Unit unit){
        StatementLine.put(GetNewLineId(),unit);
    }

    int GetNewLineId(){return StatementLine.size();}

    public HashMap<Integer,Unit> Parse(ArrayList<String> statements)throws Exception {
        //int position=0;
        String text = null, command = null, paramter = null;
        Unit unit = null;
        int c = 0;

        for (int position = 0; position < statements.size(); position++) {
            text = statements.get(position);
            if(text.isEmpty())
                continue;

            //预处理
            if (IsPreCompileCommand(text)) {
                ExecutePreCommand(text);
            }
            //转换
            //Label
            //text.toLowerCase();

            switch (text.trim()) {
                case "endfunction":
                    PushStatement(new Statement.Function.EndFcuntion(GetNewLineId()));
                    break;
                case "endloop":
                    PushStatement(new Symbol.Loop.Endloop(GetNewLineId()));
                    break;
                case "loop":
                    PushStatement(new Symbol.Loop.LoopBegin(GetNewLineId()));
                    break;
                case "break":
                    PushStatement(new Symbol.Loop.Break(GetNewLineId()));
                    break;
                case "continue":
                    PushStatement(new Symbol.Loop.Continue(GetNewLineId()));
                    break;
                case "endif":
                    PushStatement(new Symbol.Condition.EndIf(GetNewLineId()));
                    break;
                case "else":
                    PushStatement(new Symbol.Condition.Else(GetNewLineId()));
                    break;
                case "then":
                    PushStatement(new Symbol.Condition.Then(GetNewLineId()));
                    break;
                default: {
                    unit=null;
                    command = new String();
                    for (int tmp_position = 0; tmp_position < text.length(); tmp_position++) {
                        c = text.charAt(tmp_position);
                        if (c == ' ') {
                            paramter = text.substring(tmp_position + 1);
                            unit = CommandCoverToUnit(command, paramter);
                            /*
                            if(unit==null)
                                throw new Exception(String.format("Cant parse command:%s",command));*/
                            break;
                        } else {
                            command += (char)c;
                        }
                    }
                    if (unit != null) {
                        PushStatement(unit);
                        break;
                    }
                    command = new String();
                    //if(x+1)
                    for (int tmp_position = 0; tmp_position < text.length(); tmp_position++) {
                        c = text.charAt(tmp_position);
                        if (c == '(') {
                            paramter = text.substring(tmp_position);
                            unit = CommandCoverToStatement(command, paramter);
                            /*
                            if(unit==null)
                                throw new Exception(String.format("Cant parse command:%s",command));*/
                            break;
                        } else {
                            command += (char)c;
                        }
                    }
                    if(unit==null)
                        throw new Exception("cant parse command :"+text);
                    else {
                        PushStatement(unit);
                        break;
                    }
                }
            }
        }
        return null;//// TODO: 2016/10/31
    }

    //if(x+1)
    Unit CommandCoverToStatement(String command,String paramter)throws Exception{
        Statement statement=null;
        Unit unit=null;
        switch (command){
            case "if":unit=(new Symbol.Condition.If(GetNewLineId(),paramter));break;
        }
        if(statement==null&&unit==null)
            return null;
        if(statement!=null)
            return statement;
        return unit;
    }

    /***/
    Unit CommandCoverToUnit(String command,String paramter)throws Exception{
        Statement statement=null;
        Unit unit=null;
        switch (command){
            case "function":statement=(new Statement.Function.FunctionBody(GetNewLineId(),paramter));break;
            //case "if":unit=(new Symbol.Condition.If(GetNewLineId(),paramter));break;
            case "call":statement=(new Statement.Call(GetNewLineId(),paramter));break;
            case "return":statement=new Statement.Return(GetNewLineId(),paramter);break;
            case "goto":statement=new Statement.Goto(GetNewLineId(),paramter);break;
        }
        if(statement==null&&unit==null)
            return null;
        if(statement!=null)
            return statement;
        return unit;
    }

    /***预处理部分 Pre**/
    private static boolean IsPreCompileCommand(String string){return string.isEmpty()?false:string.charAt(0)=='#';}

    public abstract interface ExecutorAction{
        void onExecute(String param,Parser reference_parser);
    }

    private HashMap<String,ExecutorAction> reflectionPreExecution=null;

    public void SetPreCompileExecutors(HashMap<String,ExecutorAction> ExecutorActionHashMap){reflectionPreExecution=ExecutorActionHashMap;}

    private void ExecutePreCommand(String string)throws Exception{
        if(reflectionPreExecution==null)
            return;
        int position=1,c=0;
        String command=new String(),param=new String();
        while(true){
            if(position>=string.length())
                throw new Exception("Cant parse preCompile command: "+string);
            c=string.charAt(position);
            if(c==' ')
                break;
            command+=(char)c;
            position++;
        }
        param = string.substring(position + 1);
        if(!reflectionPreExecution.containsKey(command))
            reflectionPreExecution.get(command).onExecute(param,this);
    }

    /**Context Parse**/
    void FunctionRegister()throws Exception{
        int position=-1;
        Unit unit=null;
        Stack<Statement.Function> function_stack=new Stack<>();//,else_stack=new Stack<>();
        Stack<Symbol.Condition.If> if_stack=new Stack<>();
        Stack<Symbol.Loop.LoopBegin> loop_stack=new Stack<>();
        Statement.Function function=null;
        while(true){
            position++;
            unit=StatementLine.get(position);
            if(position>=StatementLine.size())
                break;
            if(unit.GetType()== Unit.UnitType.Statement) {
                if (((Statement) unit).GetStatementType() == Statement.StatementType.Function){
                    if (((Statement.Function) unit).GetFunctionType() == Statement.Function.FunctionType.Begin) {
                        function_stack.push((Statement.Function) unit);
                    }else if(((Statement.Function) unit).GetFunctionType() == Statement.Function.FunctionType.End){
                        if(function_stack.isEmpty())
                            throw new Exception("No more \"function\" head can be matched with \"endfunction\" label");
                        if(function_stack.peek().end_line>=0)
                            throw new Exception("duplicate \"endfcuntion\" in current function statement");
                        function_stack.peek().end_line=position;
                        function=function_stack.pop();
                        FunctionTable.put(function.GetFunctionName(),function);
                    }
                }
            }else if(unit.GetType()== Unit.UnitType.Symbol){
                    if(((Symbol) unit).GetSymbolType() == Symbol.SymbolType.ConditionBranch){
                        //Condition Branch
                        if(((Symbol.Condition)unit).GetConditionType()== Symbol.Condition.ConditionType.Else){
                            if(if_stack.empty())
                                throw new Exception("No more \"if\" head can be matched with \"else\" label");
                            if(if_stack.peek().else_line>=0)
                                throw new Exception("duplicate \"else\" in current if branch");
                            if_stack.peek().else_line=position;
                        }else if (((Symbol.Condition)unit).GetConditionType()== Symbol.Condition.ConditionType.EndIf){
                            if(if_stack.empty())
                                throw new Exception("No more \"if\" head can be match with \"else\" label");
                            if_stack.peek().end_line=position;
                            if_stack.pop();
                        }else if(((Symbol.Condition)unit).GetConditionType()== Symbol.Condition.ConditionType.If){
                            if_stack.push((Symbol.Condition.If)unit);
                        }
                    }else if (((Symbol) unit).GetSymbolType() == Symbol.SymbolType.LoopBranch){
                        if(((Symbol.Loop)unit).GetLoopType()== Symbol.Loop.LoopType.LoopBegin){
                            loop_stack.push((Symbol.Loop.LoopBegin)unit);
                        }else {
                            if(loop_stack.isEmpty())
                                throw new Exception("No more \"loop\" head can be matched with \"endloop\" label");
                            if (((Symbol.Loop) unit).GetLoopType() == Symbol.Loop.LoopType.Endloop) {
                                loop_stack.peek().end_line = position;
                                loop_stack.pop();
                            } else if (((Symbol.Loop) unit).GetLoopType() == Symbol.Loop.LoopType.Break) {
                                if(((Symbol.Loop)unit).reference_loop!=null)
                                    throw new Exception("duplicate \"begin_line\" in current loop branch");
                                ((Symbol.Loop)unit).reference_loop=loop_stack.peek();
                            } else if (((Symbol.Loop) unit).GetLoopType() == Symbol.Loop.LoopType.Continue) {
                                if(((Symbol.Loop)unit).reference_loop!=null)
                                    throw new Exception("duplicate \"begin_line\" in current loop branch");
                                ((Symbol.Loop)unit).reference_loop=loop_stack.peek();
                            } else if(((Symbol) unit).GetSymbolType() == Symbol.SymbolType.LoopBranch){

                            }else
                                throw new Exception("unknown loop branch type");
                        }
                    }else if(((Symbol) unit).GetSymbolType() == Symbol.SymbolType.Label){
                        //// TODO: 2016/11/1
                    }else
                        throw new Exception("unknown symbol type");
                }else
                    throw new Exception(String.format("unknown unit type %s",unit.GetType().toString()));
            }
            return;
        }
    }