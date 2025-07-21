package admin;

import agent.*;
import constants.Const;
import java.util.*;
import rand.randomGenerator;

public class AdminOptim {
    private int n;
    private double[][] W; // Admin can operate adjacency matrix like link recommending
    private int[] followerNumArray;
    private List<Post> recommendPostQueue = new ArrayList<>();
    private int maxRecommPostQueueLength = Const.MAX_RECOMMENDATION_POST_LENGTH;

    public AdminOptim(int userNum, double[][] W) {
        this.n = userNum;
        this.W = W;
        this.followerNumArray = new int[n];
    }

    public double[][] getAdjacencyMatrix() {
        double[][] copy = new double[n][n];
        for (int i = 0; i < n; i++) {
            copy[i] = Arrays.copyOf(this.W[i], n);
        }
        return copy;
    }

    public int[] getFollowerList() {
        return this.followerNumArray.clone();
    }

    public void setW(double[][] W) {
        this.W = W.clone();
        setFollowerNumArray();
    }

    public void setFollowerNumArray() {
        Arrays.fill(this.followerNumArray, 0);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (this.W[i][j] > 0.0) {
                    this.followerNumArray[j]++;
                }
            }
        }
    }

    public void addRecommendPost(Post post) {
        if (recommendPostQueue.size() >= this.maxRecommPostQueueLength) {
            recommendPostQueue.remove(0); // 先頭（古い投稿）を削除
        }
        recommendPostQueue.add(post); // 新しい投稿を追加
    }

    public void updateAdjacencyMatrix(int userId, int followedId, int unfollowedId) {
        if (followedId >= 0) {
            this.W[userId][followedId] = 1.0;
        }
        if (unfollowedId >= 0) {
            this.W[userId][unfollowedId] = 0.0;
        }

        setFollowerNumArray();
    }

    public void AdminFeedback(int userId, Agent[] agentSet, List<Post> latestPostList) {
        int postNum = agentSet[userId].getNumOfPosts(); // maximun of posts that an agent can read at a step

        List<Post> tempFeed = new ArrayList<>();

        for (Post post : agentSet[userId].getPostCash().getAllPosts()) {
            if (!agentSet[userId].getUnfollowList()[post.getPostUserId()]) { // just for confirmation
                tempFeed.add(post);
            }
        }

        Collections.shuffle(tempFeed, randomGenerator.rand);
        for (Post post : tempFeed) {
            agentSet[userId].addPostToFeed(post);
        }

    }
}
