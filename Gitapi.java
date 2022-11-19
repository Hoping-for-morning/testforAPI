//package java;
//import TagMessage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Gitapi {
    private int type_id;
    private int tag;
    private String task;
    private String description;
    private String url;
    private String token;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }


    public void setToken(String token) {
        this.token = token;
    }
    public int getType_id() {
        return type_id;
    }

    public void setType_id(int type_id) {
        this.type_id = type_id;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public  CredentialsProvider connect(String apiToken){
        UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(apiToken, "");
        return provider;
    }

    // 克隆远程仓库
    public  Git fromCloneRepository(String repoUrl, String cloneDir, CredentialsProvider provider) throws GitAPIException {
        Git git = Git.cloneRepository()
                .setCredentialsProvider(provider)
                .setURI(repoUrl)
                .setDirectory(new File(cloneDir)).call();
        return git;
    }

    // 读取已有仓库
    public  Repository getRepositoryFromDir(String dir) throws IOException {
        return new FileRepositoryBuilder()
                .setGitDir(Paths.get(dir, ".git").toFile())
                .build();
    }

    // 获取所有分支
    public  List<String> getAllBranch(Git git) {
        List<String> branchList = new ArrayList<>();
        try {
            List<Ref> call = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
            for (Ref ref : call) {
                String branchName = ref.getName();
                int index = branchName.lastIndexOf("/");
                branchList.add(branchName.substring(index + 1));
            }
        } catch (GitAPIException ex) {
            throw new IllegalStateException("failed to get all the branch", ex);
        }
        return branchList;
    }

    // 创建新分支
    public  void branchCreate(String name, Git git) {
        try {
            git.checkout()
                    .setName(name)
                    .setCreateBranch(true)
                    .call();
        } catch (GitAPIException ex) {
            throw new IllegalStateException("git branch create failed", ex);
        }
    }

    // pull 方法
    // 从 branch 拉取分支
    public  void pull(String branch_name, Git git) {
        try {
            git.pull()
                    .setRemote("origin")
                    .setRemoteBranchName(branch_name)
                    .setStrategy(MergeStrategy.THEIRS)
                    .call();
        } catch (GitAPIException ex) {
            throw new IllegalStateException("git pull failed", ex);
        }
    }

    // commit 方法
    public  void commit(Git git, String message, CredentialsProvider provider) throws GitAPIException {
        git.add().addFilepattern(".").call();
        git.commit()
                .setMessage(message)
                .call();
    }

    public  void push(Git git, CredentialsProvider provider) throws GitAPIException, IOException {
        push_with_branch(git,null, provider);
    }

    // push 按照分支提交
    public  void push_with_branch(Git git, String branch, CredentialsProvider provider) throws GitAPIException, IOException, IOException {
        if (branch == null) {
            branch = git.getRepository().getBranch();
        }
        git.push()
                .setCredentialsProvider(provider)
                .setRemote("origin").setRefSpecs(new RefSpec(branch)).call();
    }

    // push 按照分支提交 tag
    public  void push_with_branch_tag(Git git, String branch, CredentialsProvider provider) throws GitAPIException, IOException, IOException {
        if (branch == null) {
            branch = git.getRepository().getBranch();
        }
        git.push()
                .setCredentialsProvider(provider)
                .setRemote("origin").setRefSpecs(new RefSpec(branch)).setPushTags().call(); //Push all tags under refs/tags/*.
    }

    // 通过 revWalk 读取仓库日志
    public  List<String> getLogs(Repository repository) throws IOException {
        return getLogsSinceCommit(repository, null, null);
    }

    // 指定 commit 获取仓库日志
    public  List<String> getLogsSinceCommit(Repository repository, String commit) throws IOException {
        return getLogsSinceCommit(repository, null, commit);
    }

    // 指定 commit 和 branch 获取仓库日志
    public  List<String> getLogsSinceCommit(Repository repository, String branch, String commit) throws IOException {
        if (branch == null) {
            branch = repository.getBranch();
        }
        Ref head = repository.findRef("refs/heads/" + branch);
        List<String> commits = new ArrayList<>();
        if (head != null) {
            try (RevWalk revWalk = new RevWalk(repository)) {
                revWalk.markStart(revWalk.parseCommit(head.getObjectId()));
                for (RevCommit revCommit : revWalk) {
                    if (revCommit.getId().getName().equals(commit)) {
                        break;
                    }
                    commits.add(revCommit.getFullMessage());
                    System.out.println("\nCommit-Message: " + revCommit.getFullMessage());
                }
                revWalk.dispose();
            }
        }

        return commits;
    }

    // TODO: 2022/11/16
    // 获取某一个提交的内容
    public  void getInfo(String sha) throws IOException {
        String apiToken = "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";
        GitHub github = new GitHubBuilder().withOAuthToken(apiToken).build();
        // get a repository
        // owner + repo
        GHRepository repo = github.getRepository("Hoping-for-morning/testforAPI");
        GHCommit commit = repo.getCommit("96ea9471ca612f74f141ae4353124a6d29835059");
        // get commit info
        GHCommit.ShortInfo shortinfor = commit.getCommitShortInfo();
        System.out.println("Author: " + shortinfor.getCommitter().getName());
        System.out.println("Message: " + shortinfor.getMessage());
        System.out.println("Commit Date : " + shortinfor.getCommitDate());
        List<GHCommit.File> change_files = commit.getFiles();
        System.out.println("changed files: " + commit.getFiles().toString());
    }

    // 获取commit提交信息
    public  String getDataSql(String commitId, String path, String gitDir) {
        try {
            // 获取仓库
            Repository repository = getRepositoryFromDir(gitDir);
            // 根据所需要查询的版本号查新ObjectId
            ObjectId objId = repository.resolve(commitId);
            RevWalk walk = new RevWalk(repository);
            RevCommit revCommit = walk.parseCommit(objId);
            RevTree revTree = revCommit.getTree();
            TreeWalk treeWalk = TreeWalk.forPath(repository, path, revTree);
            ObjectId blobId = treeWalk.getObjectId(0);
            ObjectLoader loader = repository.open(blobId);
            byte[] bytes = loader.getBytes();
            String sql = "";
            if (bytes.length > 0) {
                sql = new String(bytes);
            }
            return sql;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    // TODO: 2022/11/19
    /** 项目初始化
     *
     * param: String repoUrl 传入一个项目地址，e.g. https://github.com/Hoping-for-morning/testforAPI.git
     * param: String token   传入一个项目拥有者的 token
     * Boolean: read_repo    Ture｜已有项目从本地读取   False｜需要从远程克隆
     * String: cloneDir      传入一个本地的项目路径，需要为空，不为空返回 1，可以设置成循环
     *
     * return 0              成功
     * system out            均为控制台输出, 可以给用户展示，提示 "本地仓库已建立"
     * */
    public  int init_repo(String repoUrl, String token, Boolean read_repo, String cloneDir) throws GitAPIException, IOException {

        CredentialsProvider provider = new UsernamePasswordCredentialsProvider(token, "");

        // 第一次克隆，新建本地仓库
        if (!read_repo) {
            // 指定一个仓库地址
            System.out.println("please give a dir to clone: ");
//            String cloneDir = "/tmp/test";

            // 文件不为空，可能仓库已存在无法克隆
            File file = new File(cloneDir);
            if (file.list().length > 0) {
                System.out.println("It is not an empty directory, please try another: ");
                return 1;
            }

            // 从远程仓库获取
            Git git = fromCloneRepository(repoUrl, cloneDir, provider);
            System.out.println("The frist clone, your github address: " + repoUrl);
            System.out.println("your local dir: " + cloneDir);
            // 获取所有分支
            List<String> branches = getAllBranch(git);
            System.out.println("All branches: " + branches);
        }
        // 从本地仓库读取
        else {
            // 指定一个仓库地址
            System.out.println("please give a dir to read repository: ");
            Repository repository = getRepositoryFromDir(cloneDir);
            Git git = Git.wrap(repository);

            System.out.println("read the repository, your repository: " + git.getRepository());
            System.out.println("your local dir: " + cloneDir);

            // 默认先从主分支 pull 一次
            pull("main", git);

            // 获取所有分支
            List<String> branches = getAllBranch(git);
            System.out.println("All branches: " + branches);
        }
        return 0;
    }

    //add commend
    public  void add_tag(Git git, String message, String tag, CredentialsProvider provider) throws GitAPIException {
        //git.add().addFilepattern(".").call();
        /*git.commit()
                .setMessage(message)
                .call();
*/
        git.commit().setMessage(message).call();
        git.tag().setName(tag).call();
    }

    //create_new_branch
    public  void create_new_branch(Git git,  String name, CredentialsProvider provider) throws GitAPIException {
        git.checkout()
                .setCreateBranch(true)
                .setName(name)
                .call();
    }

    //task_commit
    public void task_commit() throws IOException, GitAPIException {
        String apiToken = this.token;
        CredentialsProvider credProvider = connect(apiToken);
        // cloneDir 本地repo地址
        String cloneDir = "E://Github/testforAPI";
        String repoUrl = this.url;

        Git git = Git.wrap(getRepositoryFromDir(cloneDir));

        //push tag
        TagMessage tag_message = new TagMessage();
        //Type_id, tag and description 从前端传入，是否要加tag，
        tag_message.setType_id(this.type_id);
        tag_message.setDescription(this.description);
        String message = tag_message.setMessage();
        if(this.tag == 1) {
            add_tag(git, message, this.task, credProvider);
            push_with_branch_tag(git, "new-branch", credProvider); //branch 可以修改
        }
        else {
            commit(git, message, credProvider);
            push_with_branch(git, "new-branch", credProvider);
        }


        git.clean().call();
        git.close();


    }
}


