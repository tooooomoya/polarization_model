package agent;

import constants.Const;
import java.util.*;
import rand.randomGenerator;

public class Agent {
    private int id;
    private double opinion;
    private double tolerance;
    private double bc; // Bounded Confidence
    private double intrinsicOpinion;
    private final int NUM_OF_AGENTS = Const.NUM_OF_SNS_USER;
    private static final Random rand = randomGenerator.rand;
    private int toPost; // % of posts at a step
    private int numOfPosts; // maximum % of posts that an agent can read at a step
    private int opinionClass;
    private PostCash postCash; // posts in feeds shall be selected from cash
    private double postProb;
    private List<Post> feed = new ArrayList<>(); // timeline
    private double useProb = Const.INITIAL_MEDIA_USER_RATE;
    private double followRate;
    private boolean traitor = false; // for further simulation like using bots
    private int timeStep;
    private boolean[] followList = new boolean[NUM_OF_AGENTS];
    private boolean[] unfollowList = new boolean[NUM_OF_AGENTS];
    private int followerNum;
    private boolean used; // whether agent uses platform or not
    private int recievedLikeCount;

    // constructor
    public Agent(int agentID) {
        this.id = agentID;
        this.tolerance = Const.INITIAL_TOLERANCE;
        this.intrinsicOpinion = Math.max(-1.0, Math.min(1.0, rand.nextGaussian() * 0.6)); // norm dist
        this.opinion = this.intrinsicOpinion;
        this.bc = Const.BOUNDED_CONFIDENCE; // dynamic not static
        this.postProb = Const.INITIAL_POST_PROB;
        this.followRate = Const.INITIAL_FOLLOW_RATE;
        this.timeStep = 0;
        this.recievedLikeCount = 0;
        setNumOfPosts(10);
        setOpinionClass();
    }

    // getter methods

    public int getId() {
        return this.id;
    }

    public double getOpinion() {
        return this.opinion;
    }

    public double getIntrinsicOpinion() {
        return this.intrinsicOpinion;
    }

    public double getTolerance() {
        return this.tolerance;
    }

    public int getNumOfPosts() {
        return this.numOfPosts;
    }

    public int getToPost() {
        return this.toPost;
    }

    public int getOpinionClass() {
        return this.opinionClass;
    }

    public double getBc() {
        return this.bc;
    }

    public double getPostProb() {
        return this.postProb;
    }

    public List<Post> getFeed() {
        return this.feed;
    }

    public double getuseProb() {
        return this.useProb;
    }

    public double getFollowRate() {
        return this.followRate;
    }

    public int getFollwerNum() {
        return this.followerNum;
    }

    public PostCash getPostCash() {
        return this.postCash;
    }

    public boolean[] getFollowList() {
        return this.followList;
    }

    public boolean[] getUnfollowList() {
        return this.unfollowList;
    }

    public boolean getTraitor() {
        return this.traitor;
    }

    // setter methods

    public void setOpinion(double value) {
        this.opinion = value;
        setOpinionClass();
    }

    public void setPostProb(double value) {
        this.postProb = value;
    }

    public void setuseProb(double value) {
        this.useProb = value;
    }

    public void setBoundedConfidence(double value) {
        this.bc = value;
    }

    public void setTimeStep(int time) {
        this.timeStep = time;
    }

    public void setTolerance(double value) {
        this.tolerance = value;
    }

    public void setIntrinsicOpinion(double value) {
        this.intrinsicOpinion = value;
    }

    public void setNumOfPosts(int value) {
        this.numOfPosts = value;
        setPostCash(this.numOfPosts);
    }

    public void setPostCash(int value) {
        this.postCash = new PostCash(value);
    }

    public void setToPost(int value) {
        this.toPost = value;
    }

    public void setOpinionClass() {
        double shiftedOpinion = this.opinion + 1; // [-1,1] → [0,2]
        double opinionBinWidth = 2.0 / Const.NUM_OF_BINS_OF_OPINION;
        this.opinionClass = (int) Math.min(shiftedOpinion / opinionBinWidth, Const.NUM_OF_BINS_OF_OPINION - 1);
    }

    public void setFollowList(double[][] W) {
        for (int i = 0; i < W.length; i++) {
            if (W[this.id][i] > 0.0) {
                this.followList[i] = true;
            }
        }
    }

    public void setFollowerNum(double[][] W) {
        this.followerNum = 0;
        for (int i = 0; i < NUM_OF_AGENTS; i++) {
            if (W[i][this.id] > 0.0) {
                this.followerNum++;
            }
        }
    }

    public void setTraitor() {
        this.traitor = true;
    }

    public void addToPostCash(Post post) {
        if (post.getPostUserId() != this.id
                && !this.unfollowList[post.getPostUserId()]) {
            this.postCash.addPost(post);
        }
    }

    public void setUsed() {
        this.used = true;
    }

    public void resetUsed() {
        this.used = false;
    }

    // other methods

    public void receiveLike() {
        this.recievedLikeCount++;
    }

    public void resetPostCash() {
        this.postCash.reset();
        this.toPost = 0;
    }

    public void addPostToFeed(Post post) {
        if (!this.unfollowList[post.getPostUserId()]) {
            this.feed.add(post);
        }
    }

    public void resetFeed() {
        this.feed.clear();
    }

    public void updatePostProb() {
        // post prob is set based on the marginal utility theory
        double increment = Const.MU_PRAM * Math.log(this.recievedLikeCount * this.recievedLikeCount + 1);
        if (increment > 1.0) {
            increment = 1.0;
        }
        this.postProb += increment;

        if (this.postProb > 1.0) {
            this.postProb = 1.0;
        }
        if (this.useProb > 1.0) {
            this.useProb = 1.0;
        }
        this.recievedLikeCount = 0;
    }

    public void updateMyself() {
        double temp = 0.0;
        int postNum = 0;
        int comfortPostNum = 0;

        // read all posts in feed
        for (Post post : this.feed) {
            temp += post.getPostOpinion();

            postNum++;

            if (Math.abs(post.getPostOpinion() - this.opinion) < 0.2) {
                comfortPostNum++;
            }

            if (Math.abs(post.getPostOpinion() - this.opinion) > this.bc) {
                this.bc -= Const.DECREMENT_BC_BY_UNFOLLOW;
            }

        }

        if (postNum == 0)
            return;

        double comfortPostRate = (double) comfortPostNum / postNum;

        if (comfortPostRate > Const.COMFORT_RATE) {
            this.postProb += Const.INCREMENT_PP;
            this.useProb += Const.INCREMENT_MUR;
        } else {
            this.useProb -= Const.DECREMENT_MUR;
        }

        //// social influence

        this.opinion = this.tolerance * this.intrinsicOpinion + (1 - this.tolerance) * (temp / postNum);

        // exp : infulencer manipulation
        // use codes below instead of social influence code above
        /*
         * if ((this.id == 7 || this.id == 4) && this.timeStep > 2000 ) {
         * this.opinion += 0.01; // choose some hub users for manipulation
         * } else {
         * this.opinion = this.tolerance * this.intrinsicOpinion + (1 - this.tolerance)
         * * (temp / postNum);
         * }
         */

        ////

        if (this.opinion < -1) {
            this.opinion = -1;
        } else if (this.opinion > 1) {
            this.opinion = 1;
        }
        if (this.postProb > 1.0) {
            this.postProb = 1.0;
        } else if (this.postProb < Const.MIN_PP) {
            this.postProb = Const.MIN_PP;
        }
        if (this.useProb > 1.0) {
            this.useProb = 1.0;
        } else if (this.useProb < Const.MIN_MUR) {
            this.useProb = Const.MIN_MUR;
        }
        if (this.bc < Const.MINIMUM_BC) {
            this.bc = Const.MINIMUM_BC;
        }

        setOpinionClass();
    }

    public Post like() {
        List<Post> candidates = new ArrayList<>();
        if (this.feed.size() <= 0) {
            return null;
        }

        for (Post post : this.feed) {
            if (Math.abs(post.getPostOpinion() - this.opinion) < this.bc) {
                candidates.add(post);
            }
        }

        // choose 1 post randomly from candidates to like
        if (!candidates.isEmpty()) {
            Post likedPost = candidates.get(rand.nextInt(candidates.size()));
            likedPost.receiveLike();
            return likedPost;
        } else {
            return null;
        }
    }

    public List<Post> repost() {
        List<Post> candidates = new ArrayList<>();
        List<Post> repostedPostList = new ArrayList<>();
        if (this.feed.isEmpty()) {
            return Collections.emptyList();
        }

        for (Post post : this.feed) {
            if (Math.abs(post.getPostOpinion() - this.opinion) < this.bc) {
                candidates.add(post);
            }
        }

        if (!candidates.isEmpty()) {
            for (Post post : candidates) {
                if (rand.nextDouble() < Const.REPOST_PROB) {
                    post.receiveLike();
                    repostedPostList.add(post);
                }
            }
        } else {
            return Collections.emptyList();
        }

        return repostedPostList;
    }

    public int follow() {
        if (rand.nextDouble() > 0.1) {
            return -1;
        }

        List<Integer> candidates = new ArrayList<>();

        for (Post post : this.feed) {
            if (Math.abs(post.getPostOpinion() - this.opinion) < this.bc && !this.followList[post.getPostUserId()]
                    && !this.unfollowList[post.getPostUserId()]) {
                candidates.add(post.getPostUserId());
            }
        }

        if (!candidates.isEmpty()) {
            int followId = candidates.get(rand.nextInt(candidates.size()));
            this.followList[followId] = true;
            return followId;
        } else {
            return -1;
        }
    }

    public int unfollow() {
        if (rand.nextDouble() > 0.1) {
            return -1;
        }

        int followeeNum = 0;
        for (int i = 0; i < NUM_OF_AGENTS; i++) {
            if (this.followList[i]) {
                followeeNum++;
            }
        }
        if (this.feed.size() <= 0.0 || followeeNum <= 1) {
            return -1;
        }

        List<Integer> dislikeUser = new ArrayList<>();
        for (Post post : this.feed) {
            if (Math.abs(post.getPostOpinion() - this.opinion) > this.bc && this.followList[post.getPostUserId()]) {
                this.unfollowList[post.getPostUserId()] = true;
                this.followList[post.getPostUserId()] = false;

                return post.getPostUserId();
            }
            if (Math.abs(post.getPostOpinion() - this.opinion) > this.bc && !this.followList[post.getPostUserId()]) {
                dislikeUser.add(post.getPostUserId());
            }
        }
        if (dislikeUser.size() > 0) { // if there's nobody to unfollow, agents can also "block" others
            this.unfollowList[dislikeUser.get(rand.nextInt(dislikeUser.size()))] = true;
        }
        return -1;
    }

    public Post makePost(int step) {

        Post post;
        post = new Post(this.id, this.opinion, step);

        this.toPost = 1;

        this.postProb -= Const.POST_COST;
        if (this.postProb < Const.MIN_PP) {
            this.postProb = Const.MIN_PP;
        }

        return post;
    }

    public double decayFunc(double time) { // for the sake of convergence
        double lambda = 0.0001;
        // return Math.exp(-lambda * time);
        return 1;
    }

}
