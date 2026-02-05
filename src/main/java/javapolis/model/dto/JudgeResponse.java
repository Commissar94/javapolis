package javapolis.model.dto;

public record JudgeResponse( String status,
                             String stdout,
                             String stderr,
                             int exitCode) {

}
