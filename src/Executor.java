import java.beans.Expression;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.*;

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

        if(parser.propherty.containsKey("package_name"))
        return;
    }

    public String GetPackageName(){return parser.propherty.get("package_name");}

    public String GetPackageVersion(){return parser.propherty.get("packaget_version");}

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
        if(!parser.FunctionTable.containsKey(name)){
            if(!functionIncludeNode.containsKey(name))
                throw new Exception( name + " isnt exsit!");
            return functionIncludeNode.get(name).executor.ExecuteFunction(name,paramster);
        }
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
                                SetVariableValue(((Parser.Statement.Set)unit).variable_name,((Parser.Statement.Set)unit).variable_value);
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
        if(isTmpVariable(name))
               return TmpVariable.get(name).peek();
        try{
            return GetCalculator().GetVariable(name);
        }catch (Exception e){
            return null;
        }
    }

    public void SetVariableValue(String name,String Value)throws Exception{
        Calculator.Variable variable=GetVariable(name);

        if(variable==null){
            RegisterTmpVariable(name);
            TmpVariable.get(name).push(new Calculator.Variable(name,Value,GetCalculator()));
            return;
        }

        if(variable.variable_type== Calculator.Variable.VariableType.ExpressionVariable){
            variable.rawText=Value;
        }else{
            variable.rawText=GetCalculator().Solve(Value);
        }
    }

    public void RegisterTmpVariable(String name){
        recordTmpVariable.peek().add(name);
        TmpVariable.put(name,new Stack<>());
    }

    /**
     *  检查是否为本地(local)变量
     * */
    public boolean isTmpVariable(String variable_name){return TmpVariable.containsKey(variable_name);}


    /**/
    HashMap<String,ChildExecutorNode> executorNodeHashMap=new HashMap<>();
    HashMap<String,ChildExecutorNode> functionIncludeNode=new HashMap<>();

    public void AddChildExecutor(String input_file)throws Exception{
        ChildExecutorNode node=new ChildExecutorNode(input_file,GetCalculator());
        for(String function:node.callableFunctionName)
            functionIncludeNode.put(function,node);
        executorNodeHashMap.put(node.include_name,node);
    }

    private class ChildExecutorNode{
        private ChildExecutorNode(){}
        ChildExecutorNode(String include_name,Calculator calculator)throws Exception{
            executor=new Executor(calculator);
            executor.InitFromFile(include_name);
            callableFunctionName=executor.GetAllFunctionName();
        }
        String include_name;
        ArrayList<String> callableFunctionName;
        Executor executor;
    }

    /*加载和卸载*/
    private int reference_count=0;
    public void Link(){
        reference_count++;
    }

    public void Drop(){
        reference_count--;
        if(!IsNonReferenced())
            return;
        ArrayList<ChildExecutorNode> childExecutorNodes=new ArrayList<>(functionIncludeNode.values());
        ChildExecutorNode node=null;
        for(int i=0;i<childExecutorNodes.size();i++){
            node=childExecutorNodes.get(i);
            node.executor.Drop();
            if(node.executor.IsNonReferenced())
                functionIncludeNode.remove(node.include_name);

        }
    }

    public boolean IsNonReferenced(){return reference_count<=0;}
}
