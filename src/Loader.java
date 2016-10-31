import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by MikiraSora on 2016/10/31.
 */
public class Loader {

    public static interface ReadLineAction{
        public void ReadLine(String string);
    }

    static void LoadFromStream(InputStream inputStream,ReadLineAction action)throws Exception{
        Reader reader=new InputStreamReader(inputStream);
        int c=0;
        String text=new String();
        while((c=reader.read())>=0){
            if(c=='\t'||c=='\n')
                continue;;
            if(c=='\r'){
                action.ReadLine(text);
                text=new String();
                reader.read();//skip \n
            }else{
                text+=(char)c;
            }
        }
    }

    static void LoadFromFile(String input_path,ReadLineAction action)throws Exception{LoadFromStream(new FileInputStream(input_path),action);}
}
