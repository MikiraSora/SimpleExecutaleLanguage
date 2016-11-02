import java.beans.Expression;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by MikiraSora on 2016/10/31.
 */
public class Executor {
    private Calculator calculator=null;
    Calculator GetCalculator(){return calculator==null?calculator=new Calculator():calculator;}

    private Executor(){}
    Executor(Calculator calculator){this.calculator=calculator;}

    Parser parser=new Parser();

    public void InitFromFile(String input_file)throws Exception{
        ArrayList<String> arrayList=new ArrayList<>();
        //Loader loader=new Loader();
        Loader.LoadFromFile(input_file,new Loader.ReadLineAction(){
            @Override
            public void ReadLine(String string) {
                arrayList.add(string);
            }
        });
        parser.Parse(arrayList);
        parser.FunctionRegister();
        return;
    }

    public int GetFunctionCount(){
        return parser.FunctionTable.size();
        }

    public ArrayList<String> GetAllFunctionName(){
        ArrayList<String> allFunction=new ArrayList<>();
        for(HashMap.Entry<String, Parser.Statement.Function> pair:parser.FunctionTable.entrySet())
            allFunction.add(pair.getKey());
        return allFunction;
    }

    abstract class Signal extends Exception{
        String signal_value=null;
        Signal(String value){signal_value=value;}
    }

    class ReturnSignal extends Signal{
        ReturnSignal(String expr){super(expr);}
    }

    class EndfunctionSignal extends Signal{
        EndfunctionSignal(){super(null);}
    }

    /**执行区**/
    public String ExecuteFunction(String name, ArrayList<Calculator.Expression> paramster)throws Exception{
        if(!parser.FunctionTable.containsKey(name))
            throw new Exception( name + " isnt exsit!");
        Parser.Statement.Function function=parser.FunctionTable.get(name);
        if(paramster.size()!=function.GetParameterRequestCount())
            throw new Exception("not enough paramester to take");
        HashMap<String, Calculator.Variable> param_set=new HashMap<>();
        for(Calculator.Expression expr:paramster)
            if(expr.GetType()==Calculator.Expression.ExpressionType.Variable)
                param_set.put(((Calculator.Variable)expr).GetName(),(Calculator.Variable)expr);
        //开始执行
        PushTmpVariable(param_set);
        Parser.Unit unit=null;
        try{
            int position=function.line;
            while(true){
                position++;
                if(position>=parser.StatementLine.size())
                    throw new Exception();//// TODO: 2016/11/2
                switch (unit.GetType()){
                    case Statement:{
                        switch (((Parser.Statement)unit).GetStatementType()){
                            case Function:{
                                if(((Parser.Statement.Function)unit).GetFunctionType()== Parser.Statement.Function.FunctionType.End){
                                    //position==(((Parser.Statement.Function.EndFcuntion)unit).line;
                                    if((((Parser.Statement.Function.EndFcuntion)unit).end_line)==function.end_line)
                                        throw new EndfunctionSignal();
                                    throw new Exception("Different function end.");
                                }
                                throw new Exception("Miaomiao?");
                            }
                            case Return:{
                                throw new ReturnSignal(((Parser.Statement)unit).statement_context);
                            }

                            case Call:{
                                GetCalculator().Solve(((Parser.Statement)unit).statement_context);
                                break;
                            }
                            case Set:{
                                //// TODO: 2016/11/2
                                break;
                            }
                            case Goto:{
                                position=Integer.valueOf((((Parser.Statement)unit).statement_context));
                                break;
                            }
                        }
                        break;
                    }
                    case Symbol:{
                        switch (((Parser.Symbol)unit).GetSymbolType()){
                            case ConditionBranch:{
                                    switch (((Parser.Symbol.Condition)unit).GetConditionType()){
                                        case If:{
                                            BooleanCaculator booleanCaculator=new BooleanCaculator(GetCalculator());
                                            if(!booleanCaculator.Solve(((Parser.Symbol.Condition.If)unit).condition)){
                                                position=((Parser.Symbol.Condition.If)unit).else_line;
                                            }
                                            break;
                                        }
                                    }
                                break;
                            }
                            case LoopBranch:{
                                switch (((Parser.Symbol.Loop)unit).GetLoopType()){
                                    case Continue:
                                    case Endloop:
                                        position=((Parser.Symbol.Loop)unit).reference_loop.line;
                                        break;

                                    case Break:
                                        position=((Parser.Symbol.Loop)unit).end_line;
                                        break;
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
                skip:{
                    continue;
                }
            }
        }catch (ReturnSignal e){return GetVariable(e.signal_value).Solve();}catch (Exception e){throw e;}finally {PopTmpVariable();}
        //
        //return null;//// TODO: 2016/11/1
    }


    Stack<ArrayList<String>> recordTmpVariable=new Stack<>();
    HashMap<String,Stack<Calculator.Variable>> TmpVariable=new HashMap<>();


    private void PushTmpVariable(HashMap<String, Calculator.Variable> variableHashMap){
        ArrayList<String> recordList=new ArrayList<>();
        for(HashMap.Entry<String, Calculator.Variable> pair:variableHashMap.entrySet())
        {
            recordList.add(pair.getKey());
            if(!TmpVariable.containsKey(pair.getKey()))
                TmpVariable.put(pair.getKey(),new Stack<>());
            TmpVariable.get(pair.getKey()).push(pair.getValue());
            //Log.Debug(String.format("tmp variable \"%s\" was push",pair.getValue().toString()));
        }
        recordTmpVariable.push(recordList);
        //Log.Debug(String.format("there are %d tmp variables are pushed in %d layout",recordList.size(),recordTmpVariable.size()));
    }

    private void PopTmpVariable()throws Exception{
        ArrayList<String> recordList=recordTmpVariable.pop();
        for(String tmp_name:recordList){
            TmpVariable.get(tmp_name).pop();
            //Log.Debug(String.format("tmp variable \"%s\" was pop",TmpVariable.get(tmp_name).pop().toString()));
            if(TmpVariable.get(tmp_name).empty())
                TmpVariable.remove(tmp_name);
        }
        //Log.Debug(String.format("there are %d tmp variables are popped in %d layout",recordList.size(),recordTmpVariable.size()+1));
    }

    private Calculator.Variable GetTmpVariable(String name){
        if(TmpVariable.containsKey(name))
            return TmpVariable.get(name).peek();
        return null;
    }


    /*
    查询变量的顺序,低到高
    /--+
    lv1 :当前代码文件区域
    lv2 :GetCalculator::GetVariable() -> lv3 :计算器本体区域
                                         lv4 :脚本遍历查询 -> lv5 :脚本本地代码区域(RequestVariable()) -> lv6 : 引用(include)的子脚本遍历查询 ..... 
    /---
    * */
    //// TODO: 2016/11/2 变量缓存机制
    public Calculator.Variable GetVariable(String name)throws Exception{
           return null;//// TODO: 2016/11/2
    }
}
