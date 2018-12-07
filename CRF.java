import java.util.ArrayList;
import java.util.HashMap;

public class CRF {
    private ArrayList<int[]> Unigram;
    private ArrayList<int[]> Bigram;
    private char[] labels;
    private String[] train_sentences;
    private String[] train_labels;
    private String[] train_output;
    private String[] test_sentences;
    private String[] test_labels;
    private String[] test_output;
    private HashMap<Integer, HashMap<String, Integer>> Unigram_map, Bigram_map;
    private int[] tag2index;
    private int[] index2tag = new int[]{'B', 'I', 'E', 'S', 'N'};

    public CRF(String[] path) {
        labels = Utils.read_labels(path[0]);

        ArrayList<ArrayList<int[]>> tmp_template = Utils.create_template(path[1]);
        Unigram = tmp_template.get(0);
        Bigram = tmp_template.get(1);

        Unigram_map = new HashMap<>();
        Bigram_map = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            HashMap<String, Integer> tmp = new HashMap<>();
            Unigram_map.put(i, tmp);
        }

        for (int i = 0; i < 25; i++) {
            HashMap<String, Integer> tmp = new HashMap<>();
            Bigram_map.put(i, tmp);
        }


        String[][] tmp = Utils.read_tarin_data("src/train_helper/train.utf8");

        train_sentences = tmp[0];
        train_labels = tmp[1];
        train_output = tmp[2];
        test_sentences = tmp[3];
        test_labels = tmp[4];
        System.out.println(train_labels[9]);
        test_output = tmp[5];
        init(train_sentences);
//        System.out.println(Bigram_map.get(0).get("交1。3"));

        int sum = sum_labels_num();
        System.out.println(sum);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < train_sentences.length; j++) {
                train_output[j] = get_output_tag(train_sentences[j]);
                update_map(train_sentences[j], train_labels[j], train_output[j]);
            }
            for (int j = 0; j < test_sentences.length; j++) {
                test_output[j] = get_output_tag(test_sentences[j]);
            }
            System.out.println((double) right_labels_num() / sum);
        }
    }

    public int right_labels_num() {
        int right_sum = 0;
        for (int i = 0; i < test_sentences.length; i++) {
            String train_label = test_labels[i];
            String output_label = test_output[i];
//            System.out.println(test_sentences[i]);
//            System.out.println(train_label);
//            System.out.println(output_label);
            for (int j = 2; j < train_label.length() - 2; j++) {
                if (train_label.charAt(j) == output_label.charAt(j))
                    right_sum++;
            }
        }
        return right_sum;
    }

    public int sum_labels_num() {
        int sum = 0;
        for (String train_label : test_labels) {
            sum -= 4;
            sum += train_label.length();
        }
        return sum;
    }

    public String viterbi(String sentence){
        int len = sentence.length() - 4;
        int[][] score = new int[4][len];
        int[][] pre_status = new int[4][len];
        int[] pre_score = new int[4];
        StringBuilder output = new StringBuilder("NN");
        StringBuilder window = new StringBuilder("  ");
        for (int i = 0; i < 4; i++) {
            window.append(sentence.charAt(i));
        }
        int tmp_score = 0;
        for (int i = 4; i < len + 4; i++) {
            window.deleteCharAt(0);
            window.append(sentence.charAt(i));
            for (int this_status = 0; this_status < 4; this_status++) {
                ArrayList<String> template_Unigram = get_template(window.toString(), Unigram);
                ArrayList<String> template_Bigram = get_template(window.toString(), Bigram);



                int[] helper = new int[4];
                for (int pre_tag = 0; pre_tag < 4; pre_tag++) {
                    int pre = pre_score[pre_tag];
                    int biscore = get_biscore(template_Bigram, pre_tag, this_status);
                    int uniscore = get_uniscore(template_Unigram, this_status);
                    helper[pre_tag] = pre + biscore + uniscore;
                }
                int max_index = get_max_index(helper);
                score[this_status][i - 4] = helper[max_index];
                pre_status[this_status][i - 4] = max_index;
            }
            for (int j = 0; j < 4; j++) {
                pre_score[j] = score[j][i - 4];
            }
        }
        int tmp = get_max_index(pre_score);
        for (int i = len - 1; i >= 0; i--) {
            output.insert(2, (char)index2tag[pre_status[tmp][i]]);
            tmp = pre_status[tmp][i];
        }

        output.append("NN");
        return output.toString();
    }

    private int get_biscore(ArrayList<String> template_Bigram, int prev_status, int this_status){
        int score = 0;
        for (String s : template_Bigram) {
            if (Bigram_map.get(this_status + 4 * prev_status).containsKey(s)){
                score += Bigram_map.get(this_status + 4 * prev_status).get(s);
            }
            else
                Bigram_map.get(this_status + 4 * prev_status).put(s, 0);
        }
        return score;
    }

    private int get_uniscore(ArrayList<String> template_Unigram, int this_status){
        int tmp_score = 0;
        for (String s : template_Unigram) {
            if (Unigram_map.get(this_status).containsKey(s)){
                tmp_score += Unigram_map.get(this_status).get(s);
            }
            else
                Unigram_map.get(this_status).put(s, 0);
        }
        return tmp_score;
    }

    private int get_max_index(int[] ints){
        int max_value = 0;
        int max_index = 0;
        for (int i = 0; i < ints.length; i++) {
            if (max_value < ints[i]){
                max_value = ints[i];
                max_index = i;
            }
        }
        return max_index;
    }

    private static int get_max(int[][] ints){
        int max_value = 0;
        int[] max_index = new int[2];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (max_value < ints[i][j]){
                    max_value = ints[i][j];
                    max_index[0] = i;
                    max_index[1] = j;
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            ints[4][i] = ints[max_index[0]][i];
        }
        return max_index[0];
    }

    public String get_output_tag(String sentence) {
        int[][] score = new int[4][sentence.length() - 4];
        int[] tag = new int[sentence.length() - 4];
        Integer tmp_value = 0;
        StringBuilder output_tag = new StringBuilder("NN");
        StringBuilder window = new StringBuilder(" ");
        for (int i = 0; i < 4; i++) {
            window.append(sentence.charAt(i));
        }
        int tmp_score = 0;
        int pre_tag = 4;
        for (int i = 4; i < sentence.length(); i++) {
            window.deleteCharAt(0);
            window.append(sentence.charAt(i));
//            System.out.println(window.length());
//            System.out.println(window);
            for (int j = 0; j < 4; j++) {
                ArrayList<String> template_Unigram = get_template(window.toString(), Unigram);
                ArrayList<String> template_Bigram = get_template(window.toString(), Bigram);
                tmp_score += get_uniscore(template_Unigram, j);
                tmp_score += get_biscore(template_Bigram, pre_tag, j);
                score[j][i - 4] = tmp_score;
                tmp_score = 0;
            }
            pre_tag = get_max_index(score[0][i - 4], score[1][i - 4], score[2][i - 4], score[3][i - 4]);
            tag[i - 4] = pre_tag;
            output_tag.append((char) index2tag[pre_tag]);
        }
        output_tag.append("NN");
//        System.out.println(output_tag.toString());
        return output_tag.toString();
    }

    public void update_map(String sentence, String real_tag, String output_tag) {
        int flag;
        Integer updata_data;
        int wrong_tag, right_tag;
        String key;
//        System.out.println(real_tag);
//        System.out.println(output_tag);
        for (int i = 2; i < output_tag.length(); i++) {
            if (output_tag.charAt(i) != real_tag.charAt(i)) {
                for (int[] unigram : Unigram) {
                    for (int i1 : unigram) {
                        flag = i1;
                        right_tag = tag2index[real_tag.charAt(i - flag)];
                        wrong_tag = tag2index[output_tag.charAt(i - flag)];
                        key = get_subString(sentence, i - flag, unigram);
                        updata_data = Unigram_map.get(right_tag).get(key);
                        Unigram_map.get(right_tag).put(key, updata_data == null ? 1 : updata_data + 1);
                        updata_data = Unigram_map.get(wrong_tag).get(key);
                        Unigram_map.get(wrong_tag).put(key, updata_data == null ? -1 : updata_data - 1);
                    }
                }
                for (int[] bigram : Bigram) {
                    for (int i1 : bigram) {
                        flag = i1;
                        right_tag = tag2index[real_tag.charAt(i - flag - 1)] * 4 + tag2index[real_tag.charAt(i - flag)];
                        wrong_tag = tag2index[output_tag.charAt(i - flag - 1)] * 4 + tag2index[output_tag.charAt(i - flag)];
                        key = get_subString(sentence, i - flag, bigram);
                        updata_data = Bigram_map.get(right_tag).get(key);
                        Bigram_map.get(right_tag).put(key, updata_data == null ? 1 : updata_data + 1);
                        updata_data = Bigram_map.get(wrong_tag).get(key);
                        Bigram_map.get(wrong_tag).put(key, updata_data == null ? -1 : updata_data - 1);
                    }
                }
            }
        }

    }

    public void init(String[] sentence) {
        tag2index = new int['S' + 1];
        tag2index['B'] = 0;
        tag2index['I'] = 1;
        tag2index['E'] = 2;
        tag2index['S'] = 3;
        tag2index['N'] = 4;
//        String key = "";
//        int i = 0;
//        HashMap<String, Integer> tmp;
//        for (String aSentence : sentence) {
////            if (i++ % 1000 == 0)
////                System.out.println(i);
//            for (int j = 2; j < aSentence.length() - 2; j++) {
//                for (int[] ints : Unigram) {
//                    for (int l = 0; l < 5; l++) {
//                        tmp = Unigram_map.get(l);
//                        key = get_subString(aSentence, j, ints);
//                        tmp.put(key, 0);
//                    }
//                }
//                for (int[] ints : Bigram) {
//                    for (int l = 0; l < 25; l++) {
//                        tmp = Bigram_map.get(l);
//                        key = get_subString(aSentence, j, ints);
//                        tmp.put(key, 0);
//                    }
//                }
//            }
//        }
    }

    private int get_max_index(int int0, int int1, int int2, int int3) {
        int index = 0;
        int max = int0;
        if (max < int1) {
            index = 1;
            max = int1;
        }
        if (max < int2) {
            index = 2;
            max = int2;
        }
        if (max < int3) {
            index = 3;
        }
        return index;
    }

    private String get_subString(String sentence, int index, int[] offset) {
        StringBuilder result = new StringBuilder();
        for (int i1 : offset) {
            result.append(sentence.charAt(index + i1)).append(i1 + 2);
        }
        return result.toString();
    }

    private ArrayList<String> get_template(String window, ArrayList<int[]> template) {
        ArrayList<String> list = new ArrayList<>();
        for (int[] ints : template) {
            list.add(get_subString(window, 2, ints));
        }
        return list;
    }
}
