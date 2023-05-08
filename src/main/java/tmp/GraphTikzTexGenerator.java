package tmp;

import freemarker.template.Configuration;
import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModelEx;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.apache.velocity.Template;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author strike
 */
public class GraphTikzTexGenerator {

    static String url = "jdbc:h2:file:/home/strike/Documentos/doutorado/artigo-p3-hull-heuristica/db-resultado-heuristica/db";

    static String sqlquery = "SELECT distinct k, min(tss) as tss "
            + " FROM resultado  WHERE k <= 6 and algoritmo in ('ALGO') and grafo='GRAFO' and p='OP' "
            + "  group by grupo, grafo, p, k, algoritmo "
            + " order by k";

    public static void main(String... args) throws ClassNotFoundException, SQLException {
        String[] dataSets = new String[]{
            "ca-GrQc", "ca-HepTh",
            "ca-CondMat",
            "ca-HepPh",
            "ca-AstroPh",
            "Douban",
            "Delicious",
            "BlogCatalog3",
            "BlogCatalog2",
            "Livemocha",
            "BlogCatalog",
            "BuzzNet",
            "Last.fm", //            "YouTube2"
        };
        String[] operacoes = new String[]{
            "m", //            "k", //            "r"
        };
        init();
        Configuration cfg = new Configuration();
        try {
            //Load template from source folder
            cfg.setTemplateLoader(new TemplateAbsolutePathLoader());
            Template template = cfg.getTemplate("/home/strike/Documentos/doutorado/artigo-greedy-contamination-problem/grafico-big-exec-linhas.tex.vm");
            // Build the data-model
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("grafos", dataSets);
            context.put("operacoes", operacoes);
            GetList getlist = new GraphTikzTexGenerator.GetList();

            context.put("getlist", getlist);
            context.put("legend", Map.of("m", "percentage of neighbors"));
            // Console output
            Writer out = new OutputStreamWriter(System.out);
            template.process(context, out);
            out.flush();

//            // File output
//            Writer file = new FileWriter(new File("C:\\FTL_helloworld.txt"));
//            template.process(data, file);
//            file.flush();
//            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        end();
    }
    static Connection conn = null;
    static Statement stmt = null;

    private static void init() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        conn = DriverManager.getConnection(url, "", "");
        stmt = conn.createStatement();
    }

    private static void end() throws SQLException {
//        stmt.close();
        conn.close();
    }

    public static class GetList implements TemplateMethodModelEx {

        @Override
        public Object exec(List args) {
            StringBuilder sb = new StringBuilder();
//            StringBuilder sbs = new StringBuilder();

//            sb.append("'");
            try {
                List<SimpleScalar> argv = args;
                String op = argv.get(1).getAsString();
                String sql = sqlquery
                        .replaceAll("GRAFO", argv.get(0).getAsString())
                        .replaceAll("OP", op)
                        .replaceAll("ALGO", argv.get(2).getAsString());
                ResultSet executeQuery
                        = stmt.executeQuery(sql);

                while (executeQuery.next()) {
                    int k = executeQuery.getInt("k");
                    int tss = executeQuery.getInt("tss");
                    sb.append("(");
                    if (op.equals("m")) {
                        sb.append("0." + k);
                    } else {
                        sb.append(k);
                    }
                    sb.append(",");
                    sb.append(tss);
                    sb.append(") ");
                }

            } catch (Exception ex) {
                Logger.getLogger(GraphTikzTexGenerator.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
//            sb.append("'");
//            sbs.append("(0,3)");
//            return new SimpleScalar(sb.toString());
            return sb.toString();
        }
    }
}
