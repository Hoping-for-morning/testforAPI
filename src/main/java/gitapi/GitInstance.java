package gitapi;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitInstance implements Closeable {
    private final Git git;

    private final String repoUrl;

    private final TransportConfigCallback transportConfigCallback;



    public GitInstance(Git git, String repoUrl, TransportConfigCallback transportConfigCallback) {

        this.git = git;

        this.repoUrl = repoUrl;

        this.transportConfigCallback = transportConfigCallback;

    }



    public String getRepoUrl() {

        return repoUrl;

    }



    public File getWorkTree() {

        return git.getRepository().getWorkTree();

    }



    public RevCommit commit(String message) {

        try {

            return git.commit()

                    .setMessage(message)

                    .setAll(true)

                    .call();

        } catch (GitAPIException ex) {

            throw new IllegalStateException("git commit failed", ex);

        }

    }



    public void merge(String name) {

        try {

            ObjectId ref = git.getRepository().resolve(name);

            git.merge()

                    .include(ref)

                    .setStrategy(MergeStrategy.THEIRS)

                    .call();

        } catch (GitAPIException | IOException ex) {

            throw new IllegalStateException("git merge failed", ex);

        }

    }



    public void pull(String name) {

        try {

            git.pull()

                    .setRemote("origin")

                    .setRemoteBranchName(name)

                    .setStrategy(MergeStrategy.THEIRS)

                    .setTransportConfigCallback(transportConfigCallback)

                    .call();

        } catch (GitAPIException ex) {

            throw new IllegalStateException("git pull failed", ex);

        }

    }



    public void branchCreate(String name) {

        try {

            git.checkout()

                    .setName(name)

                    .setCreateBranch(true)

                    .call();

        } catch (GitAPIException ex) {

            throw new IllegalStateException("git branch create failed", ex);

        }

    }



    public void add(String filePattern) {

        try {

            git.add()

                    .addFilepattern(filePattern)

                    .call();

        } catch (GitAPIException ex) {

            throw new IllegalStateException("git add failed", ex);

        }

    }



    public void addAll() {

        add(".");

    }



    public void tag(String name) {

        try {

            git.tag()

                    .setName(name)

                    .call();

        } catch (GitAPIException ex) {

            throw new IllegalStateException("git tag failed", ex);

        }

    }



    public void push() {

        try {

            git.push()

                    .setPushAll()

                    .setTransportConfigCallback(transportConfigCallback)

                    .call();

        } catch (GitAPIException ex) {

            throw new IllegalStateException("git push failed", ex);

        }

    }



    public void checkout(String branchName) {

        try {

            git.checkout()

                    .setName(branchName)

                    .call();

        } catch (GitAPIException ex) {

            throw new IllegalStateException("git checkout branch failed", ex);

        }

    }



    public void checkoutRemote(String name) {

        try {

            git.checkout()

                    .setName(name)

                    .setStartPoint("origin/" + name)

                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)

                    .setCreateBranch(true)

                    .call();

        } catch (GitAPIException ex) {

            throw new IllegalStateException("git checkout remote branch failed", ex);

        }

    }



    public List<String> getAllBranch() {

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



    public List<String> getFileCommitLog(String file) {

        List<String> commitIdList = new ArrayList<>();

        try {

            Iterable<RevCommit> call = git.log().addPath(file).call();

            for (RevCommit revCommit : call) {

                String[] split = revCommit.getId().toString().split(" ");

                String commitId = split[1].substring(0, 8);

                commitIdList.add(commitId);

            }

            return commitIdList;

        } catch (GitAPIException ex) {

            throw new IllegalStateException("failed to get branch", ex);

        }

    }



    @Override

    public void close() {

        git.close();

    }



    public Repository getRepository() {

        return git.getRepository();

    }

}