import java.util.ArrayList;

/**
 * Created by MikiraSora on 2016/10/31.
 */
public class Executor {

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

    }

    public int GetFunctionCount(){
        return 0;//// TODO: 2016/10/31
        }
}
