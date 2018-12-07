import java.util.Arrays;
import java.util.HashMap;

public class Train {
    public static void main(String[] args) {
        CRF crf = new CRF(new String[]{"src/train_helper/labels.utf8", "src/train_helper/my_template.utf8"});
        String[] sentence = Utils.read_tarin_data("src/train_helper/template.utf8")[0];
        String[] labels = Utils.read_tarin_data("src/train_helper/template.utf8")[1];
        for (int i = 0; i < sentence.length; i++) {
            if (sentence[i].length() == 1){
                System.out.print(sentence[i - 1]);
                System.out.println(i);
            }
        }
        System.out.println(sentence.length);
    }
}
