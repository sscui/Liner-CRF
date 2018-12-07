import java.io.*;
import java.util.ArrayList;

public class Utils {

    public static ArrayList<String> read_file(String path){
        ArrayList<String> arrayList = new ArrayList<>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(new File(path)));
            String str;
            while((str = br.readLine()) != null){
                arrayList.add(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public static char[] read_labels(String path){
        char[] labels;
        ArrayList<String> tmp_label = Utils.read_file(path);
        int size = tmp_label.size();
        labels = new char[size];
        for (int i = 0; i < size; i++) {
            labels[i] = tmp_label.get(i).charAt(0);
        }
        return labels;
    }

    public static ArrayList<ArrayList<int[]>> create_template(String path){
        ArrayList<int[]> unigram = new ArrayList<>();
        ArrayList<int[]> bigram = new ArrayList<>();
        ArrayList<ArrayList<int[]>> result = new ArrayList<>();
        result.add(unigram);
        result.add(bigram);

        BufferedReader br= null;
        try {
            br = new BufferedReader(new FileReader(new File(path)));
            String str = null;
            while((str = br.readLine()) != null){
                if (str.startsWith("U") || str.startsWith("B")) {
                    ArrayList<Integer> tmp = parse_template(str);
                    int[] ints = new int[tmp.size() - 1];
                    for (int i = 0; i < ints.length; i++) {
                        ints[i] = tmp.get(i + 1);
                    }
                    result.get(tmp.get(0)).add(ints);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static ArrayList<Integer> parse_template(String string){
        ArrayList<Integer> template = new ArrayList<>();
        template.add(string.charAt(0) == 'U' ? 0 : 1);
        string = string.split(":")[1];
        String[] tmp = string.split("/");
        for (String s : tmp) {
            int a = Integer.parseInt(s.substring(3, s.length() - 3));
            template.add(a);
        }
        return template;
    }

    public static String[][] read_tarin_data(String path){
        ArrayList<String> list_sentence = new ArrayList<>();
        ArrayList<String> list_labels = new ArrayList<>();
        ArrayList<String> list_output = new ArrayList<>();
        StringBuilder sentence = new StringBuilder("  ");
        StringBuilder labels = new StringBuilder("NN");
        StringBuilder output = new StringBuilder("NN");
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(new File(path)));
            String str;
            while((str = br.readLine()) != null){
                if (str.length() == 3){
                    sentence.append(str.charAt(0));
                    labels.append(str.charAt(2));
                    output.append("B");
                }else{
                    sentence.append("  ");
                    labels.append("NN");
                    output.append("BB");
                    list_sentence.add(sentence.toString());
                    list_labels.add(labels.toString());
                    list_output.add(output.toString());
                    sentence.setLength(2);
                    labels.setLength(2);
                    output.setLength(2);
                }
            }
            sentence.append("  ");
            labels.append("NN");
            output.append("NN");
            list_sentence.add(sentence.toString());
            list_labels.add(labels.toString());
            list_output.add(output.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int size = list_sentence.size();
        int tmp_len = size - size / 5;
        String[][] tmp = new String[6][tmp_len];
        for (int i = 0; i < tmp_len; i++) {
            tmp[0][i] = list_sentence.get(i);
            tmp[1][i] = list_labels.get(i);
            tmp[2][i] = list_output.get(i);
        }
        tmp[3] = new String[size - tmp_len];
        tmp[4] = new String[size - tmp_len];
        tmp[5] = new String[size - tmp_len];
        for (int i = tmp_len; i < size; i++) {
            tmp[3][i - tmp_len] = list_sentence.get(i);
            tmp[4][i - tmp_len] = list_labels.get(i);
            tmp[5][i - tmp_len] = list_output.get(i);
        }
        return tmp;
    }
}
