import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class example {
    public static void main(String[] args) throws GitAPIException, IOException {
        // six parameters
        int tag = 0; // 0: no tag; 1: add tag
        String task_name = "task";
        int type_id = 0; //0~9
        String description = "description";
        String url = "https://github.com/Hoping-for-morning/testforAPI.git";
        String token = "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";

        //  call Gitapi
        Gitapi gitapi = new Gitapi();
        gitapi.setTag(tag);
        gitapi.setTask(task_name);
        gitapi.setType_id(type_id);
        gitapi.setDescription(description);
        gitapi.setUrl(url);
        gitapi.setToken(token);

        gitapi.task_commit();
    }

}
