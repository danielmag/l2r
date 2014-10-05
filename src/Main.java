import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class Main
{
    private double paramC = 0.0D;
    private double paramG = 0.0D;
    private int paramEpochs = 0;
    private int paramNodes = 0;
    private int paramRounds = 0;
    private int paramTc = 0;
    private int paramRandomRestart = 0;
    private int paramIteration = 0;
    private double MAP = 0.0D;
    private String algorithm = new String();

    public Main() {}

    public double increaseCValue(double C, String kernel)
    {
        if (kernel.equals("LINEAR")) {
            C += 0.1D;
        }
        if (kernel.equals("NONLINEAR")) {
            double exponent = Math.log(C) / Math.log(2.0D);


            exponent += 2.0D;


            C = Math.pow(2.0D, exponent);
        }
        return C;
    }

    public double increaseGValue(double G)
    {
        double exponent = Math.log(G) / Math.log(2.0D);


        exponent += 2.0D;


        G = Math.pow(2.0D, exponent);

        return G;
    }

    public int increaseParameter(int nodes)
    {
        nodes++;
        return nodes;
    }

    public void saveModels(String path)
    {
        for (int i = 1; i <= 4; i++)
        {
            File model = new File("./" + path + "/Fold" + i + "/model.bin");
            (new File("./" + path + "/Fold" + i + "/model_final.bin")).delete();
            while(!model.renameTo(new File("./" + path + "/Fold" + i + "/model_final.bin"))) {
                //throw new RuntimeException("Could not rename model.bin");
                System.out.println("ciclo");
            }
        }
    }

    public int executeCommand(String cmd, String path){

        Process process;
        int exitVal = -1;
        BufferedWriter writer = null;
        DecimalFormat df = new DecimalFormat("0.00000");


        try {

            process = Runtime.getRuntime().exec(cmd);

            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;

            String metric = "MAP = ";  // MAP =

            while((line=input.readLine()) != null){

                System.out.println( line);

                if(cmd.contains("generate-treceval-files.jar") && line.contains(metric)){


                    double result = Double.parseDouble(line.replace(metric, ""));

                    if(algorithm.equalsIgnoreCase("SVMRANKLINEAR")){
                        writer = new BufferedWriter(new FileWriter("results"+algorithm+".txt", true));
                        writer.write(paramC + "\t" + df.format(result) + "\n");
                        if(result > MAP) {
                            MAP = result;
                            saveModels(path);
                        }
                    }

                    if(algorithm.equalsIgnoreCase("SVMRANKNONLINEAR")){
                        writer = new BufferedWriter(new FileWriter("results"+algorithm+".txt", true));
                        writer.write(paramC + "\t" + paramG + "\t" + df.format(result) + "\n");
                        if(result > MAP) {
                            MAP = result;
                            saveModels(path);
                        }
                    }

                    if(algorithm.equalsIgnoreCase("RANKNET")){
                        writer = new BufferedWriter(new FileWriter("results"+algorithm+".txt", true));
                        writer.write(paramEpochs + "\t" + paramNodes + "\t" + df.format(result) + "\n");
                        if(result > MAP) {
                            MAP = result;
                            saveModels(path);
                        }
                    }

                    if(algorithm.equalsIgnoreCase("ADARANK")){
                        writer = new BufferedWriter(new FileWriter("results"+algorithm+".txt", true));
                        writer.write(paramRounds + "\t" + df.format(result) + "\n");
                        if(result > MAP) {
                            MAP = result;
                            saveModels(path);
                        }
                    }

                    if(algorithm.equalsIgnoreCase("RANKBOOST")){
                        writer = new BufferedWriter(new FileWriter("results"+algorithm+".txt", true));
                        writer.write(paramRounds + "\t" + paramTc +"\t"+ df.format(result) + "\n");
                        if(result > MAP) {
                            MAP = result;
                            saveModels(path);
                        }
                    }
                    if(algorithm.equalsIgnoreCase("COORDINATEASCENT")){
                        writer = new BufferedWriter(new FileWriter("results"+algorithm+".txt", true));
                        writer.write(paramRandomRestart + "\t" + paramIteration +"\t"+ df.format(result) + "\n");
                        if(result > MAP) {
                            MAP = result;
                            saveModels(path);
                        }
                    }
                    writer.close();
                }
            }

            exitVal = process.waitFor();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return exitVal;
    }

    public void runCoordAscent(String type, int totalRR, int totalIterations){
        
        algorithm = "coordinateAscent";
        
        for(int randomRestart = 1; randomRestart <= totalRR; randomRestart++){
            for(int iteration = 1; iteration <= totalIterations; iteration++){
                
                paramIteration = iteration;
                paramRandomRestart = randomRestart;
                
                System.out.println("\n###################################################");
                System.out.println("The number of iterations is currently " + iteration);
                System.out.println("###################################################\n");
                
                for(int fold = 1; fold <= 4; fold++){
                
                    String trainFilePath = "./"+type+"/Fold"+fold+"/train";
                    String testFilePath = "./"+type+"/Fold"+fold+"/test";
                    String modelFilePath = "./"+type+"/Fold"+fold+"/model.bin";
                    String predictionFile = "./"+type+"/Fold"+fold+"/prediction";
                    String rankBoost = "./Learning_to_Rank_Algorithms/RankLib.jar -ranker 4";
                    
                    //start learning process
                    System.out.println("java -jar " + rankBoost + " -train " + trainFilePath + " -metric2t MAP " + " -test " + testFilePath + " -metric2T MAP " + " -i " + iteration + " -r " + randomRestart + " -save " + modelFilePath);
                    executeCommand("java -jar " + rankBoost + " -train " + trainFilePath + " -metric2t MAP " + " -test " + testFilePath + " -metric2T MAP " + " -i " + iteration + " -r " + randomRestart  + " -save " + modelFilePath, "");
                    
                    //starting classification process
                    executeCommand("java -jar " + rankBoost + " -rank " + testFilePath + " -metric2T MAP " + " -load " + modelFilePath +" -results " + predictionFile, "");
                }
                
                //evaluate folds
                executeCommand("java -jar generate-treceval-files.jar " + type, type);
            }
        }
        deleteGeneratedFiles(type);
    }

    public void runCoordAscent(String type, int totalRR, int totalIterations, String features){
        
        algorithm = "coordinateAscent";
        
        for(int randomRestart = 1; randomRestart <= totalRR; randomRestart++){
            for(int iteration = 1; iteration <= totalIterations; iteration++){
                
                paramIteration = iteration;
                paramRandomRestart = randomRestart;
                
                System.out.println("\n###################################################");
                System.out.println("The number of iterations is currently " + iteration);
                System.out.println("###################################################\n");
                
                for(int fold = 1; fold <= 4; fold++){
                
                    String trainFilePath = "./"+type+"/Fold"+fold+"/train";
                    String testFilePath = "./"+type+"/Fold"+fold+"/test";
                    String modelFilePath = "./"+type+"/Fold"+fold+"/model.bin";
                    String predictionFile = "./"+type+"/Fold"+fold+"/prediction";
                    String rankBoost = "./Learning_to_Rank_Algorithms/RankLib.jar -ranker 4";
                    
                    //start learning process
                    System.out.println("java -jar " + rankBoost + " -train " + trainFilePath + " -metric2t MAP " + " -test " + testFilePath + " -metric2T MAP " + " -feature " + features + " -i " + iteration + " -r " + randomRestart + " -save " + modelFilePath);
                    executeCommand("java -jar " + rankBoost + " -train " + trainFilePath + " -metric2t MAP " + " -test " + testFilePath + " -metric2T MAP " + " -feature " + features + " -i " + iteration + " -r " + randomRestart  + " -save " + modelFilePath, "");
                    
                    //starting classification process
                    executeCommand("java -jar " + rankBoost + " -rank " + testFilePath + " -metric2T MAP " + " -load " + modelFilePath + " -feature " + features + " -results " + predictionFile, "");
                }
                
                //evaluate folds
                executeCommand("java -jar generate-treceval-files.jar " + type, type);
            }
        }
        deleteGeneratedFiles(type);
    }

    public void runRankBoost(String type, int totalRounds, int totalCandidates){
        
        algorithm = "RankBoost";
        
        for(int round = 1; round <= totalRounds; round++){
            for(int tc = 1; tc <= totalCandidates; tc++){
                
                paramRounds = round;
                paramTc = tc;
                
                System.out.println("\n###################################################");
                System.out.println("The number of iterations is currently " + round);
                System.out.println("###################################################\n");
                
                for(int fold = 1; fold <= 4; fold++){
                
                    String trainFilePath = "./"+type+"/Fold"+fold+"/train";
                    String testFilePath = "./"+type+"/Fold"+fold+"/test";
                    String modelFilePath = "./"+type+"/Fold"+fold+"/model.bin";
                    String predictionFile = "./"+type+"/Fold"+fold+"/prediction";
                    String rankBoost = "./Learning_to_Rank_Algorithms/RankLib.jar -ranker 2";
                    
                    //start learning process
                    System.out.println("java -jar " + rankBoost + " -train " + trainFilePath + " -metric2t MAP " + " -test " + testFilePath + " -metric2T MAP " + " -round " + round + " -tc " + tc + " -save " + modelFilePath);
                    executeCommand("java -jar " + rankBoost + " -train " + trainFilePath + " -metric2t MAP " + " -test " + testFilePath + " -metric2T MAP " + " -round " + round + " -tc " + tc + " -save " + modelFilePath, "");
                    
                    //starting classification process
                    executeCommand("java -jar " + rankBoost + " -rank " + testFilePath + " -metric2T MAP " + " -load " + modelFilePath +" -results " + predictionFile, "");
                }
                
                //evaluate folds
                executeCommand("java -jar generate-treceval-files.jar " + type, type);
            }
        }
        deleteGeneratedFiles(type);
    }

    public void runAdaRank(String type, int totalRounds){
        
        algorithm = "AdaRank";
        
        for(int round = 1; round <= totalRounds; round++){
            
            paramRounds = round;
            
            System.out.println("\n###################################################");
            System.out.println("The number of iterations is currently " + round);
            System.out.println("###################################################\n");
            
            for(int fold = 1; fold <= 4; fold++){
            
                String trainFilePath = "./"+type+"/Fold"+fold+"/train";
                String testFilePath = "./"+type+"/Fold"+fold+"/test";
                String modelFilePath = "./"+type+"/Fold"+fold+"/model.bin";
                String predictionFile = "./"+type+"/Fold"+fold+"/prediction";
                String adaRank = "./Learning_to_Rank_Algorithms/RankLib.jar -ranker 3";
                
                //start learning process
                System.out.println("java -jar " + adaRank + " -train " + trainFilePath + " -metric2t MAP " + " -test " + testFilePath + " -metric2T MAP " + " -round " + round + " -save " + modelFilePath);
                executeCommand("java -jar " + adaRank + " -train " + trainFilePath + " -metric2t MAP " + " -test " + testFilePath + " -metric2T MAP " + " -round " + round + " -save " + modelFilePath, "");
                
                //starting classification process
                executeCommand("java -jar " + adaRank + " -rank " + testFilePath + " -metric2T MAP " + " -load " + modelFilePath +" -results " + predictionFile, "");
            }
            
            //evaluate folds
            executeCommand("java -jar generate-treceval-files.jar " + type, type);
            
        }
        deleteGeneratedFiles(type);
    }

    public void runRankNet(String type, int totalEpochs, int totalNodes){
        
        int nodes = 0;
        algorithm = "RankNet";
        
        for(int epochs = 1; epochs <= totalEpochs; epochs++){
        
            for( nodes = 1; nodes <= totalNodes; nodes++){
            
                paramEpochs = epochs;
                paramNodes = nodes;
                
                System.out.println("\n###################################################");
                System.out.println("The number of nodes is currently " + nodes);
                System.out.println("###################################################\n");
                
                for(int fold = 1; fold <= 4; fold++){
                
                    String trainFilePath = "./"+type+"/Fold"+fold+"/train";
                    String testFilePath = "./"+type+"/Fold"+fold+"/test";
                    String modelFilePath = "./"+type+"/Fold"+fold+"/model.bin";
                    String predictionFile = "./"+type+"/Fold"+fold+"/prediction";
                    String rankNet = "./Learning_to_Rank_Algorithms/RankLib.jar -ranker 1";
                    
                    //start learning process
                    System.out.println("java -jar " + rankNet + " -train " + trainFilePath + " -metric2t MAP " + " -test " + testFilePath + " -metric2T MAP " + " -node " + nodes + " -epoch " +epochs + " -save " + modelFilePath);
                    executeCommand("java -jar " + rankNet + " -train " + trainFilePath + " -metric2t MAP " + " -test " + testFilePath + " -metric2T MAP " + " -node " + nodes + " -epoch " +epochs + " -save " + modelFilePath, "");
                    
                    //starting classification process
                    executeCommand("java -jar " + rankNet + " -rank " + testFilePath + " -metric2T MAP " + " -load " + modelFilePath +" -results " + predictionFile, "");
                }
                
                //evaluate folds
                executeCommand("java -jar generate-treceval-files.jar " + type, type);
            }
        }
        deleteGeneratedFiles(type);
    }

    public void deleteGeneratedFiles(String path)
    {
        for (int i = 1; i <= 4; i++)
        {
            File file = new File(path + "/Fold" + i + "/model.bin");
            file.delete();
            file = new File(path + "/Fold" + i + "/qRel");
            file.delete();
            file = new File(path + "/Fold" + i + "/qResults");
            file.delete();
            file = new File(path + "/Fold" + i + "/prediction");
            file.delete();
            file = new File(path + "/Fold" + i + "/model_final.bin");
            file.renameTo(new File(path + "/Fold" + i + "/model.bin"));
        }
    }

    public void runSVMrankLinear(String type, double totalC)
    {
        this.algorithm = "SVMrankLinear";
        double C = 0.0D;
        while (C <= totalC)
        {
            C = increaseCValue(C, "LINEAR");
            this.paramC = C;

            System.out.println("\n###################################################\n");
            System.out.println("The C value is currently " + C);
            System.out.println("###################################################\n");
            for (int fold = 1; fold <= 4; fold++)
            {
                executeCommand("./Learning_to_Rank_Algorithms/svm-rank/svm_rank_learn -c " + C + " " + type + "/Fold" + fold + "/train " + type + "/Fold" + fold + "/model.bin", "");


                executeCommand("./Learning_to_Rank_Algorithms/svm-rank/svm_rank_classify " + type + "/Fold" + fold + "/test " + type + "/Fold" + fold + "/model.bin " + type + "/Fold" + fold + "/prediction", "");
            }
            executeCommand("java -jar ./generate-treceval-files.jar " + type, type);
        }
        deleteGeneratedFiles(type);
    }

    public void runSVMrankNonLinear(String type, double totalC, double totalGamma)
    {
        this.algorithm = "SVMrankNonLinear";
        double C = Math.pow(2.0D, -5.0D);
        double G = Math.pow(2.0D, -5.0D);

        this.paramC = C;
        this.paramG = G;
        while (C <= totalC)
        {
            System.out.println("\n###################################################\n");
            System.out.println("The C value is currently " + C);
            System.out.println("###################################################\n");
            while (G <= totalGamma)
            {
                System.out.println("\n###################################################\n");
                System.out.println("The G value is currently " + G);
                System.out.println("###################################################\n");
                for (int fold = 1; fold <= 4; fold++)
                {
                    executeCommand("./Learning_to_Rank_Algorithms/svm-rank/svm_rank_learn -t 2 -c " + C + " -g " + G + " " + type + "/Fold" + fold + "/train " + type + "/Fold" + fold + "/model.bin", "");


                    executeCommand("./Learning_to_Rank_Algorithms/svm-rank/svm_rank_classify " + type + "/Fold" + fold + "/test " + type + "/Fold" + fold + "/model.bin " + type + "/Fold" + fold + "/prediction", "");
                }
                G = increaseGValue(G);
                this.paramG = G;
            }
            executeCommand("java -jar ./generate-treceval-files.jar " + type, type);

            C = increaseCValue(C, "NONLINEAR");
            this.paramC = C;

            G = Math.pow(2.0D, -5.0D);
            this.paramG = G;
        }
        deleteGeneratedFiles(type);
    }

    public void waitTime(int seconds)
    {
        long t0 = System.currentTimeMillis();
        long t1;
        do
        {
            t1 = System.currentTimeMillis();
        } while (t1 - t0 < seconds * 1000);
    }

    public static void main(String[] args)
    {
        Main search = new Main();
        if (args.length < 2)
        {
            System.out.println("Usage: ");
            System.out.println("java -jar grid-search.jar SVMrankLinear <path> <c_param>");
            System.out.println("java -jar grid-search.jar SVMrankNonLinear <path> <c_param> <g_param>");
            System.out.println("java -jar grid-search.jar RankNet <path> <epochs_param> <nodes_param>");
            System.out.println("java -jar grid-search.jar AdaRank <path> <iterations_param>");
            System.out.println("java -jar grid-search.jar RankBoost <path> <iterations_param> <threshold_candidates_param>");
            System.out.println("java -jar grid-search.jar Coordinate_Ascent <path> <randomRestarts_param> <iterations_param>\n");
            System.exit(0);
        }
        if (args[0].equalsIgnoreCase("SVMRANKLINEAR")) {
            search.runSVMrankLinear(args[1], Double.parseDouble(args[2]));
        }
        if (args[0].equalsIgnoreCase("SVMRANKNONLINEAR")) {
            search.runSVMrankNonLinear(args[1], Double.parseDouble(args[2]), Double.parseDouble(args[3]));
        }
        if (args[0].equalsIgnoreCase("RANKNET")) {
            search.runRankNet(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        }
        if (args[0].equalsIgnoreCase("ADARANK")) {
            search.runAdaRank(args[1], Integer.parseInt(args[2]));
        }
        if (args[0].equalsIgnoreCase("RANKBOOST")) {
            search.runRankBoost(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        }
        if (args[0].equalsIgnoreCase("COORDINATE_ASCENT")) {
            search.runCoordAscent(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        }
    }
}
