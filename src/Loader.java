import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by MikiraSora on 2016/10/31.
 */
public class Loader {

    abstract class ReadLineAction{
        void ReadLine(String string){}
    }

    static void LoadFromStream(InputStream inputStream,ReadLineAction action)throws Exception{
        Reader reader=new InputStreamReader(inputStream);
        int c=0;
        String text=new String();
        while((c=reader.read())>=0){
            if(c=='\n'){
                action.ReadLine(text);
                text=new String();
            }else{
                text+=(char)c;
            }
        }
    }

    static void LoadFromFile(String input_path,ReadLineAction action)throws Exception{LoadFromStream(new FileInputStream(input_path),action);}
}
