package javapolis.service;

import javapolis.model.dto.JudgeRequest;
import javapolis.model.dto.JudgeResponse;
import org.springframework.stereotype.Service;

@Service
public class JudgeService {

    public JudgeResponse judge(JudgeRequest request) {
        // временная заглушка
        return new JudgeResponse(
                "OK",
                "Hello from judge 👋",
                "",
                0
        );
    }
}