package javapolis.controller;

import javapolis.model.dto.JudgeRequest;
import javapolis.model.dto.JudgeResponse;
import javapolis.service.JudgeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/judge")
class JudgeController {


    private final JudgeService judgeService;

    public JudgeController(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    @PostMapping("/solutions")
    public JudgeResponse judge(@RequestBody JudgeRequest request) {
        return judgeService.judge(request);
    }

}
