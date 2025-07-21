package dynamics;

import admin.*;
import agent.*;
import analysis.*;
import constants.Const;
import gephi.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import network.*;
import rand.randomGenerator;
import writer.Writer;

public class OpinionDynamics {
    private final int t = Const.MAX_SIMULATION_STEP;
    private final int agentNum = Const.NUM_OF_SNS_USER;
    private Network network;
    private final Agent[] agentSet = new Agent[agentNum];
    private Writer writer;
    private Analysis analyzer;
    private AssertionCheck ASChecker;
    private final String[] resultList = Const.RESULT_LIST;
    private final String folerPath = Const.RESULT_FOLDER_PATH;
    private GraphVisualize gephi;
    private RepostVisualize repostGephi;
    private double connectionProbability = Const.CONNECTION_PROB_OF_RANDOM_NW;
    private AdminOptim admin;
    private static final Random rand = randomGenerator.rand;
    private int[][] repostNetwork;

    // constructor
    public OpinionDynamics() {
        setFromInitial();
        // setCustomized(); // when start with a existing gexf file
        this.analyzer = new Analysis();
        this.writer = new Writer(folerPath, resultList);
        this.gephi = new GraphVisualize(0.00, agentSet, network);
        this.repostGephi = new RepostVisualize(agentSet);
        this.admin = new AdminOptim(agentNum, network.getAdjacencyMatrix());
        this.repostNetwork = new int[agentSet.length][agentSet.length];
    }

    private void setFromInitial() {
        setNetwork();
        setAgents();
    }

    private void setNetwork() {
        ///// you can change the initial network bellow
        // this.network = new RandomNetwork(agentNum, connectionProbability);
        this.network = new ConnectingNearestNeighborNetwork(agentNum, 0.3);
        /////

        this.network.makeNetwork(agentSet);
        System.out.println("finish making network");
    }

    private void setAgents() {
        double[][] tempAdjacencyMatrix = this.network.getAdjacencyMatrix();
        for (int i = 0; i < agentNum; i++) {
            agentSet[i] = new Agent(i);
            agentSet[i].setFollowList(tempAdjacencyMatrix);
            agentSet[i].setFollowerNum(tempAdjacencyMatrix);

            // make sure that initial NW has opinion leaders in every opinion-sides (used
            // only when starting with CNN NW)
            switch (agentSet[i].getId()) {
                case 0 -> {
                    agentSet[i].setIntrinsicOpinion(0.0);
                }
                case 1 -> {
                    agentSet[i].setIntrinsicOpinion(-1.0);
                }
                case 2 -> {
                    agentSet[i].setIntrinsicOpinion(1.0);
                }
                default -> {
                }
            }
        }
    }

    private void setCustomized() {
        this.network = new ReadNetwork(agentNum, Const.READ_NW_PATH);
        this.network.makeNetwork(agentSet);
        System.out.println("finish making network");

        double[][] tempAdjacencyMatrix = this.network.getAdjacencyMatrix();
        for (int i = 0; i < agentNum; i++) {
            agentSet[i] = new Agent(i);
            agentSet[i].setFollowList(tempAdjacencyMatrix);
        }
        GephiReader.readGraphNodes(agentSet, Const.READ_NW_PATH);
    }

    private void errorReport() {
        ASChecker.reportASError();
    }

    // main part of the experimental dynamics
    public void evolve() {
        this.ASChecker = new AssertionCheck(agentSet, network, agentNum, t);
        // export gexf
        gephi.updateGraph(agentSet, network);
        gephi.exportGraph(0, folerPath);

        // export metrics
        writer.setSimulationStep(0);
        writer.setOpinionVar(analyzer.computeVarianceOpinion(agentSet));
        writer.setOpinionBins(agentSet);
        writer.write();
        writer.writeDegrees(network.getAdjacencyMatrix(), folerPath);

        int followActionNum;
        int unfollowActionNum;
        List<Post> latestPostList = new ArrayList<>();
        int latestListSize = Const.LATEST_POST_LIST_LENGTH;

        for (int step = 1; step <= t; step++) {
            System.out.println("step = " + step);
            followActionNum = 0;
            unfollowActionNum = 0;

            analyzer.clearPostCash();
            analyzer.resetFeedMap();
            writer.clearPostBins();
            writer.setSimulationStep(step);
            double[][] W = admin.getAdjacencyMatrix();
            List<Post> postList = new ArrayList<>();

            for (Agent agent : agentSet) {
                int agentId = agent.getId();
                agent.setFollowerNum(W);
                agent.setTimeStep(step);
                agent.resetUsed();

                // decide whether to use platform at this step
                if (rand.nextDouble() > agent.getuseProb()) {
                    continue;
                }
                agent.setUsed();

                // admin sets user's feed
                admin.AdminFeedback(agentId, agentSet, latestPostList);
                analyzer.setFeedMap(agent);
                agent.updatePostProb();

                List<Post> repostedPostList = agent.repost();
                for (Post repostedPost : repostedPostList) {
                    repostNetwork[agentId][repostedPost.getPostUserId()]++;
                    for (Agent otherAgent : agentSet) {
                        if (W[otherAgent.getId()][agentId] > 0.00) { // add posts to followers' feeds
                            otherAgent.addToPostCash(repostedPost);
                        }
                    }
                    agentSet[repostedPost.getPostUserId()].receiveLike();
                }

                /////// follow
                int followedId = agent.follow();

                /////// unfollow
                int unfollowedId = agent.unfollow();

                /////// post
                if (rand.nextDouble() < agent.getPostProb()) {
                    Post post = agent.makePost(step);
                    for (Agent otherAgent : agentSet) {
                        if (W[otherAgent.getId()][agentId] > 0.00) {
                            otherAgent.addToPostCash(post);
                        }
                    }
                    writer.setPostBins(post);
                    analyzer.setPostCash(post);
                    postList.add(post);
                    if (latestPostList.size() > latestListSize - 1) {
                        latestPostList.remove(0);
                    }
                    latestPostList.add(post);
                }

                agent.updateMyself();
                admin.updateAdjacencyMatrix(agentId, followedId, unfollowedId);
                agent.resetPostCash();
                agent.resetFeed();
                ASChecker.assertionChecker(agentSet, admin, agentNum, step);
                if (followedId >= 0) {
                    followActionNum++;
                }
                if (unfollowedId >= 0) {
                    unfollowActionNum++;
                }
            }

            if (step % 1000 == 0) {
                // export gexf
                network.setAdjacencyMatrix(admin.getAdjacencyMatrix());
                gephi.updateGraph(agentSet, network);
                gephi.exportGraph(step, folerPath);
                repostGephi.updateGraph(agentSet, repostNetwork, step);
                repostGephi.exportGraph(step, folerPath);
                for (int[] repostNetwork1 : repostNetwork) {
                    Arrays.fill(repostNetwork1, 0);
                }
                writer.writeDegrees(W, folerPath);
                writer.writeClusteringCoefficients(analyzer.computeClusteringCoefficients(W), folerPath);
            }
            // export metrics
            writer.setOpinionVar(analyzer.computeVarianceOpinion(agentSet));
            analyzer.computePostVariance();
            writer.setPostOpinionVar(analyzer.getPostOpinionVar());
            writer.setFollowUnfollowActionNum(followActionNum, unfollowActionNum);
            writer.setOpinionBins(agentSet);
            writer.setOpinionAvg(analyzer.computeMeanOpinion(agentSet));
            analyzer.computeFeedMetrics(agentSet);
            writer.setFeedMeanArray(analyzer.getFeedMeanArray());
            writer.setFeedVarArray(analyzer.getFeedVarArray());
            writer.write();
        }
    }

    public static void main(String[] args) {
        Instant start = Instant.now();

        OpinionDynamics simulator = new OpinionDynamics();
        simulator.evolve();

        Instant end = Instant.now();

        Duration timeElapsed = Duration.between(start, end);

        // print some major information about the simulation parameter

        simulator.errorReport();

        System.out.println("Start time:     " + start);
        System.out.println("End time:       " + end);
        System.out.println("Elapsed time:   " + timeElapsed.toMillis() + " ms");
    }
}
