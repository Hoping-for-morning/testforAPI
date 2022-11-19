package gitapi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Gitapi {
    public static CredentialsProvider connect(String email, String password, String apiToken){
        UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(apiToken, "");
        return provider;
    }

    // 克隆远程仓库
    public static Git fromCloneRepository(String repoUrl, String cloneDir, CredentialsProvider provider) throws GitAPIException {
        Git git = Git.cloneRepository()
                .setCredentialsProvider(provider)
                .setURI(repoUrl)
                .setDirectory(new File(cloneDir)).call();
        return git;
    }

    // 读取已有仓库
    public static Repository getRepositoryFromDir(String dir) throws IOException {
        return new FileRepositoryBuilder()
                .setGitDir(Paths.get(dir, ".git").toFile())
                .build();
    }

    // commit 方法
    public static void commit(Git git, String message, CredentialsProvider provider) throws GitAPIException {
        git.add().addFilepattern(".").call();
        git.commit()
                .setMessage(message)
                .call();
    }

    public static void push(Git git, CredentialsProvider provider) throws GitAPIException, IOException {
        push_with_branch(git,null, provider);
    }

    public static void push_with_branch(Git git, String branch, CredentialsProvider provider) throws GitAPIException, IOException, IOException {
        if (branch == null) {
            branch = git.getRepository().getBranch();
        }
        git.push()
                .setCredentialsProvider(provider)
                .setRemote("origin").setRefSpecs(new RefSpec(branch)).call();
    }

    // 通过 revWalk 读取仓库日志
    public static List<String> getLogs(Repository repository) throws IOException {
        return getLogsSinceCommit(repository, null, null);
    }

    public static List<String> getLogsSinceCommit(Repository repository, String commit) throws IOException {
        return getLogsSinceCommit(repository, null, commit);
    }

    public static List<String> getLogsSinceCommit(Repository repository, String branch, String commit) throws IOException {
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

    public static void main(String[] args) throws GitAPIException, IOException {
        String apiToken = "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";

        CredentialsProvider credProvider = Gitapi.connect(email, password, apiToken);

        String cloneDir = "/tmp/test";
        String repoUrl = "https://github.com/Hoping-for-morning/testforAPI.git";
//        String repoUrl = "git@github.com:Hoping-for-morning/testforAPI.git";

        // 从远程仓库获取
//        Git git = Gitapi.fromCloneRepository(repoUrl, cloneDir, credProvider);
        Git git = Git.wrap(Gitapi.getRepositoryFromDir(cloneDir));

        String yaml = "dependencies:\n" +
                "- name: springboot-rest-demo\n" +
                "  version: 0.0.5\n" +
                "  repository: http://hub.hubHOST.com/chartrepo/ainote\n" +
                "  alias: demo\n" +
                "- name: exposecontroller\n" +
                "  version: 2.3.82\n" +
                "  repository: http://chartmuseum.jenkins-x.io\n" +
                "  alias: cleanup\n";

        // 修改文件
        FileWriter writer;
        try {
            writer = new FileWriter(cloneDir + "/requirement.yaml");
            writer.write(yaml);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // commit
        Gitapi.commit(git, "write yaml", credProvider);

        Gitapi.push_with_branch(git, "edit", credProvider);

        git.clean().call();
        git.close();


    }
}
