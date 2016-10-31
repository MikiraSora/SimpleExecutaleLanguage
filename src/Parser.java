import java.util.ArrayList;
import java.util.HashMap;


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
            Unknown
        }

        String current_paramster="";

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
            private Function(){super();}
            Function(int line,String body){super(line,body);}

            @Override
            public StatementType GetStatementType() {
                return StatementType.Function;
            }

            static enum FunctionType{
                Begin,End,Unknown
            }

            public FunctionType GetFunctionType(){return FunctionType.Unknown;}

            static class FunctionBody extends Function{
                private FunctionBody(){}
                FunctionBody(int line,String body){super(line,body);}

                @Override
                public FunctionType GetFunctionType() {
                    return FunctionType.Begin;
                }
            }

            static class EndFcuntion extends Function{
                private EndFcuntion(){}
                EndFcuntion(int line){super(line,"");}

                @Override
                public FunctionType GetFunctionType() {
                    return FunctionType.End;
                }
            }
        }

        static class Call extends Statement{
            private Call(){}
            Call(int line,String name,){super(line,name);}

            @Override
            public StatementType GetStatementType() {
                return StatementType.Call;
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
            private Condition(){}
            Condition(int line){super(line);}

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
            private Loop(){}
            Loop(int line){super(line);}

            @Override
            public SymbolType GetSymbolType() {
                return SymbolType.LoopBranch;
            }

            enum LoopType{
               LoopBegin,Continue,Break,Endloop,Unknown
            }

            public LoopType GetLoopType(){return LoopType.Unknown;}

            static class LoopBegin extends Loop{
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
    /***Cache****/
    HashMap<String,Integer> LabelPositionCache=new HashMap<>();
    HashMap<String,Integer> FunctionPositionCache=new HashMap<>();

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

    public HashMap<Integer,Unit> Parse(ArrayList<String> statements)throws Exception{
        //int position=0;
        String text=null,command=null,paramter=null;
        Statement unit=null;
        int c=0;

        for(int position=0;position<statements.size();position++){
            text=statements.get(0);
            //预处理
            if(IsPreCompileCommand(text)) {
                ExecutePreCommand(text);
            }
            //转换
            //Label
            text.toLowerCase();
            switch (text){
                //case "endfunction":PushStatement(new Symbol.Condition.Endfunction(GetNewLineId()));break;
                case "endloop":PushStatement(new Symbol.Loop.Endloop(GetNewLineId()));break;
                case "loop":PushStatement(new Symbol.Loop.LoopBegin(GetNewLineId()));break;
                case "break":PushStatement(new Symbol.Loop.Break(GetNewLineId()));break;
                case "continue":PushStatement(new Symbol.Loop.Continue(GetNewLineId()));break;
                case "endif":PushStatement(new Symbol.Condition.EndIf(GetNewLineId()));break;
                case "else":PushStatement(new Symbol.Condition.Else(GetNewLineId()));break;
                case "then":PushStatement(new Symbol.Condition.Then(GetNewLineId()));break;
                default:{
                    command=new String();
                    for (int tmp_position = 0; tmp_position < text.length(); tmp_position++) {
                        c = text.charAt(position);
                        if (c == ' ') {
                            paramter = text.substring(tmp_position + 1);
                            unit=CommandCoverToUnit(command,paramter);
                            if(unit==null)
                                throw new Exception(String.format("Cant parse command:%s",command));
                            break;
                        } else {
                            command += c;
                        }
                    }
                }
            }
        }

        return null;
    }

    /***/
    Statement CommandCoverToUnit(String command,String paramter){
        switch (command){
            case "function":PushStatement(new Statement.Function.FunctionBody(GetNewLineId(),paramter));break;
            case "if":PushStatement(new Symbol.Condition.If(GetNewLineId(),paramter));break;
            case "call":PushStatement(new Statement.Call(GetNewLineId(),paramter));break;
        }
        return null;
    }

    /***预处理部分 Pre**/
    private static boolean IsPreCompileCommand(String string){return string.charAt(0)=='#';}

    public abstract class ExecutorAction{
        void onExecute(String param,Parser reference_parser){}
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
}