package sql.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by duhaiguang on 2017/7/18.
 */
public class SQLFormatterV2 {


    String [] splitSql(String s,String tab){
        String flag = "\001";
        String tmp = s.replaceAll("\\s+"," ")
                .replaceAll("(?i) AND ",flag+tab+"AND ")
                .replaceAll("(?i) BETWEEN ",flag+tab+"BETWEEN ")
                .replaceAll("(?i) CASE ",flag+tab+"CASE ")
                .replaceAll("(?i) ELSE ",flag+tab+"ELSE ")
                .replaceAll("(?i) END ",flag+tab+"END ")
                .replaceAll("(?i) FROM ",flag+"FROM ")
                .replaceAll("(?i) GROUP\\s+BY ",flag+"GROUP BY ")
                .replaceAll("(?i) HAVING ",flag+"HAVING ")
                .replaceAll("(?i) SET "," SET"+flag)
                .replaceAll("(?i) IN "," IN ")
                .replaceAll("(?i) JOIN ",flag+"JOIN ")
                .replaceAll("(?i) INNER"+flag+"+JOIN ",flag+"INNER JOIN"+flag)
                .replaceAll("(?i) LEFT"+flag+"+JOIN ",flag+"LEFT JOIN"+flag)
                .replaceAll("(?i) LEFT\\s+SEMI"+flag+"+JOIN ",flag+"LEFT SEMI JOIN"+flag)
                .replaceAll("(?i) LEFT\\s+OUTER"+flag+"+JOIN ",flag+"LEFT OUTER JOIN"+flag)
                .replaceAll("(?i) RIGHT\\s+OUTER"+flag+"+JOIN ",flag+"RIGHT OUTER JOIN"+flag)
                .replaceAll("(?i) RIGHT"+flag+"+JOIN ",flag+"RIGHT JOIN"+flag)
                .replaceAll("(?i) ON "," ON ")
                .replaceAll("(?i) OR ",flag+tab+"OR ")
                .replaceAll("(?i) ORDER\\s+BY ",flag+"ORDER BY ")
                .replaceAll("(?i) OVER ",flag+tab+"OVER ")
                .replaceAll("(?i)\\(\\s*SELECT ",flag+"("+"SELECT ")
                .replaceAll("(?i)\\)\\s*SELECT ",")"+flag+"SELECT ")
                .replaceAll("(?i) THEN "," THEN"+" ")
                .replaceAll("(?i) UNION ",flag+"UNION"+flag)
                .replaceAll("(?i)"+flag+"+UNION"+flag+"+ALL ",flag+"UNION ALL"+flag)
                .replaceAll("(?i) USING ",flag+"USING ")
                .replaceAll("(?i) WHEN ",flag+tab+"WHEN ")
                .replaceAll("(?i) WHERE ",flag+"WHERE ")
                .replaceAll("(?i) WITH ",flag+"WITH ")
                .replaceAll("(?i) ALL "," ALL ")
                .replaceAll("(?i) AS "," AS ")
                .replaceAll("(?i) ASC "," ASC ")
                .replaceAll("(?i) DESC "," DESC ")
                .replaceAll("(?i) DISTINCT "," DISTINCT ")
                .replaceAll("(?i) EXISTS "," EXISTS ")
                .replaceAll("(?i) NOT "," NOT ")
                .replaceAll("(?i) NULL "," NULL ")
                .replaceAll("(?i) LIKE "," LIKE ")
                .replaceAll("(?i) SET "," SET ")
                .replaceAll("(?i)INSERT ","INSERT ")
                .replaceAll("(?i) INTO "," INTO ")
                .replaceAll("(?i) TABLE "," TABLE ")
                .replaceAll("(?i) OVERWIRTE "," OVERWRITE ")
                .replaceAll("(?i) PARTITION"," PARTITION ")
                .replaceAll("(?i) PARTITION\\("," PARTITION(")
                .replaceAll("(?i)\\s*SELECT ","SELECT ")
                .replaceAll("(?i)\\s*UPDATE ","UPDATE ")
                .replaceAll(";",";"+flag)
                .replaceAll(flag+"+",flag);
        //System.out.println(tmp);
        return tmp.split(flag);
    }

    private int getNextLevel(String s, int parenthesisLevel){
        return parenthesisLevel - s.replaceAll("\\(","").length() + s.replaceAll("\\)","").length();
    }
    private boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        boolean answer =  matcher.matches();
        //System.out.println(regex + "###" + str + "###" + answer);
        return answer ;
    }

    private String shift(int n){
        String ret = "\n";
        for(int i = 0; i<n; i++){
            ret += "    ";
        }
        return ret;
    }
    public String format(String s){

        String flag = "\001";
        String[] sArray = s.replaceAll("\'",flag+"'").split(flag);
        System.out.println("$$$");
        for(String t: sArray){
            System.out.println(t);
        }
        System.out.println("$$$");

        List<Node>nodeList = new ArrayList<Node>();
        for (int i = 0;i<sArray.length;i++){
            if(i%2==1){
                nodeList.add(new Node(NodeType.CONTENT,sArray[i]));
            }else{
                for (String temp : splitSql(sArray[i].replaceAll("\\s+"," "), "    ")){
                    nodeList.add(new Node(NodeType.CODE, temp));
                }
            }
        }
        String result = "";
        int deep = 0;
        int parentLevel = 0;
        for(Node node: nodeList){
            String newText = node.text;
            if (node.type == NodeType.CONTENT){
                newText = node.text;
            }else{

                String text = node.text;

                //System.out.println("before == "+ text);
                if (match(".*;\\s*SELECT\\s+.*",text)){
                    text = text.replaceAll(";\\s*SELECT\\s+",";\n SELECT ");
                }
                if (match(".*\\)\\s*SELECT\\s+.*",text)){
                    text = text.replaceAll("\\)\\s*SELECT\\s+",")\n SELECT ");
                }
                if (match("\\(\\s*SELECT\\s+.*",text)){
                    deep++;
                    newText = shift(deep-1)+"("+shift(deep) + text.substring(1);
                    parentLevel = getNextLevel(text, parentLevel);
                }else{
                    if(text.startsWith("'")){
                        newText = text;
                    }else {
                        newText = shift(deep) + text;
                    }

                    parentLevel = getNextLevel(text, parentLevel);
                    if( deep > 0 && parentLevel < deep) deep --;
                }

                System.out.println(deep + " "+ parentLevel +" "+text);
            }
            result += newText;

        }
        return result;
    }

        public static void main(String[]args){

        String sql ="insert into table study_dmp.ykt_tag_value partition(day='{2}', module='{4}')\n" +
                "    select * from(\n" +
                "        select distinct t1.userid, '{3}', value from\n" +
                "        (\n" +
                "            select userid, userid as value from study_dr.user_behavior where day>='{0}' and day<'{1}' and activename like '%user_login%'\n" +
                "            union all\n" +
                "            select userid, userid as value from study_dr.mooc_app where day>='{0}' and day<'{1}' and action like '%user_login%'\n" +
                "        )t1 left outer join\n" +
                "        (\n" +
                "            select userid from study_dr.user_behavior where day>='{1}' and day<='{2}' and activename like '%user_login%'\n" +
                "            union all\n" +
                "            select userid from study_dr.mooc_app where day>='{1}' and day<='{2}' or action like '%user_login%'\n" +
                "        )t2 on t1.userid = t2.userid where t2.userid is null\n" +
                "    )a left semi join study_dr.dwu_member b on a.userid = b.id and b.day = '{2}'";
        System.out.println(new SQLFormatterV2().format(sql));
//        String [] t = new Test().split(sql,"\t");
//        for (String s : t){
//            System.out.println(s);
//        }
//        System.out.println("");
    }
    class Node{
        NodeType type;
        String text;

        public Node(NodeType type, String text){
            this.type = type;
            this.text = text;
        }
    }

    enum NodeType{
        CONTENT, CODE
    }
}

