package constants;

public class Const {
    // simulation parameter
    public static final int MAX_SIMULATION_STEP = 5000;
    public static final int NUM_OF_USER = 1000;
    public static final int NUM_OF_SEED_USER = (int) (0.003 * NUM_OF_USER);
    public static final int NUM_OF_SNS_USER = NUM_OF_USER;
    public static final int RANDOM_SEED = 1896;

    // Admin feedback parameter
    public static final int MAX_RECOMMENDATION_POST_LENGTH = 100;
    public static final int LATEST_POST_LIST_LENGTH = 200;

    // network parameter
    public static final double CONNECTION_PROB_OF_RANDOM_NW = 0.01;
    
    // agent parameter
    public static final double BOUNDED_CONFIDENCE = 1.0; // initial bc
    public static final double MINIMUM_BC = 0.2;
    public static final double REPOST_PROB = 0.4;
    public static final double POST_COST = 0.5;
    public static final double MU_PARAM = 0.1; // Marginal Utility log func parameter

    // user num = 1000
    public static final double INITIAL_CNN_SEED_GRAPH_CONNECT_PROB = 0.2;
    public static final double INITIAL_PP = 0.1; // Prob of Posting
    public static final double INITIAL_PU = 0.1; // Prob of Using platform
    public static final double INCREMENT_PP = 0.3;
    public static final double INCREMENT_PU = 0.05;
    public static final double DECREMENT_PP = 0.0;
    public static final double DECREMENT_PU = 0.05;
    public static final double MIN_PP = 0.1;
    public static final double MIN_PU = INITIAL_PU;
    public static final double DECREMENT_BC = 0.005;

    // user num = 5000
    /*public static final double INITIAL_CNN_SEED_GRAPH_CONNECT_PROB = 0.2;
    public static final double INITIAL_PP = 0.1; // Prob of Posting
    public static final double INITIAL_PU = 0.1; // Prob of Using platform
    public static final double INCREMENT_PP = 0.3;
    public static final double INCREMENT_PU = 0.05;
    public static final double DECREMENT_PP = 0.0;
    public static final double DECREMENT_PU = 0.05;
    public static final double MIN_PP = 0.1;
    public static final double MIN_PU = INITIAL_PU;
    public static final double DECREMENT_BC = 0.01;*/    

    public static final double COMFORT_RATE = 0.8;
    public static final double INITIAL_TOLERANCE = 0.8;

    // follow parameter
    public static final double FOLLOW_PROB = 0.1;

    // unfollow parameter
    public static final double UNFOLLOW_PROB = 0.1;

    // input data parameter
    public static final String EDGES_FILE_PATH = "Twitter/edgesTwitter.txt";
    public static final String OPINION_FILE_PATH = "Twitter/Twitter_opinion.txt";

    public static final String READ_NW_PATH = "results/temp/step_1000.gexf";

    // result data parameter
    public static final String[] RESULT_LIST = { "opinionVar", "postOpinionVar", "follow", "unfollow", "rewire", "opinionAvg",
    "feedPostOpinionMean_0", "feedPostOpinionMean_1", "feedPostOpinionMean_2", "feedPostOpinionMean_3", "feedPostOpinionMean_4", "feedPostOpinionVar_0",
    "feedPostOpinionVar_1", "feedPostOpinionVar_2", "feedPostOpinionVar_3", "feedPostOpinionVar_4"};
    public static final String RESULT_FOLDER_PATH = "results";
    public static final int NUM_OF_BINS_OF_POSTS = 5; // % of bins of opinions in posts for analysis
    public static final int NUM_OF_BINS_OF_OPINION = 5;
    public static final int NUM_OF_BINS_OF_OPINION_FOR_WRITER = 10;
}
