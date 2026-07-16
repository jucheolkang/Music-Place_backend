package org.musicplace.global.logging;

/**
 * MDC(Mapped Diagnostic Context)에 저장되는 Key 상수
 *
 * 모든 로그는 이 Key를 기준으로 JSON 로그를 생성한다.
 * 문자열 하드코딩을 방지하기 위해 모든 MDC Key를 이곳에서 관리한다.
 */
public final class MdcConstants {

    private MdcConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 요청 추적 ID
     * (현재는 UUID, 이후 OpenTelemetry TraceId로 변경 예정)
     */
    public static final String TRACE_ID = "traceId";

    /**
     * 요청 ID
     * (현재는 TraceId와 동일하게 사용)
     */
    public static final String REQUEST_ID = "requestId";

    /**
     * 로그인한 사용자 ID
     */
    public static final String USER_ID = "userId";

    /**
     * HTTP Method
     */
    public static final String METHOD = "method";

    /**
     * Request URI
     */
    public static final String URI = "uri";

    /**
     * HTTP Status
     */
    public static final String STATUS = "status";

    /**
     * 요청 처리 시간(ms)
     */
    public static final String ELAPSED_TIME = "elapsedTime";

}
