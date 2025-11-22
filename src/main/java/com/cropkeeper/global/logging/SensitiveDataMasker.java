package com.cropkeeper.global.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 로그 출력 시 민감정보를 자동으로 마스킹하는 유틸리티 클래스
 *
 * 마스킹 대상:
 * - password, passwd, pwd 등 비밀번호 관련 필드
 * - token, accessToken, refreshToken 등 인증 토큰
 * - contact, phone, tel 등 연락처 (뒷자리 마스킹)
 */
@Slf4j
public class SensitiveDataMasker {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 완전히 마스킹할 필드명 (대소문자 무시)
    private static final Set<String> FULLY_MASKED_FIELDS = new HashSet<>(Arrays.asList(
            "password", "passwd", "pwd",
            "currentpassword", "newpassword", "passwordconfirm", "newpasswordconfirm",
            "token", "accesstoken", "refreshtoken",
            "secret", "secretkey", "apikey"
    ));

    // 부분적으로 마스킹할 필드명 (뒷자리만 마스킹)
    private static final Set<String> PARTIALLY_MASKED_FIELDS = new HashSet<>(Arrays.asList(
            "contact", "phone", "tel", "mobile",
            "email"
    ));

    // 마스킹 문자
    private static final String MASK = "****";
    private static final int PARTIAL_MASK_START = 4; // 뒤에서 몇 자리 마스킹할지

    /**
     * 객체를 로그 출력용 문자열로 변환하면서 민감정보를 마스킹합니다.
     *
     * @param obj 마스킹할 객체
     * @return 민감정보가 마스킹된 문자열
     */
    public static String maskSensitiveData(Object obj) {
        if (obj == null) {
            return "null";
        }

        // 기본 타입은 그대로 반환
        if (isPrimitiveOrWrapper(obj)) {
            return String.valueOf(obj);
        }

        // String은 그대로 반환 (필드명 없이는 판단 불가)
        if (obj instanceof String) {
            return (String) obj;
        }

        // Collection 처리
        if (obj instanceof Collection) {
            return maskCollection((Collection<?>) obj);
        }

        // Map 처리 (중요: Request Body는 보통 Map으로 변환됨)
        if (obj instanceof Map) {
            return maskMap((Map<?, ?>) obj);
        }

        // 객체를 Map으로 변환 후 마스킹
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.convertValue(obj, Map.class);
            return maskMap(map);
        } catch (Exception e) {
            log.debug("객체 마스킹 실패, toString() 사용: {}", obj.getClass().getName());
            return obj.toString();
        }
    }

    /**
     * Map의 민감정보를 마스킹합니다.
     */
    private static String maskMap(Map<?, ?> map) {
        Map<String, Object> masked = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();

            // 민감 필드 확인 (대소문자 무시)
            String keyLower = key.toLowerCase();

            if (FULLY_MASKED_FIELDS.contains(keyLower)) {
                // 완전 마스킹
                masked.put(key, MASK);
            } else if (PARTIALLY_MASKED_FIELDS.contains(keyLower)) {
                // 부분 마스킹
                masked.put(key, maskPartially(value));
            } else if (value instanceof Map) {
                // 중첩된 Map 재귀 처리
                masked.put(key, maskMap((Map<?, ?>) value));
            } else if (value instanceof Collection) {
                // Collection 처리
                masked.put(key, maskCollection((Collection<?>) value));
            } else {
                // 일반 값은 그대로
                masked.put(key, value);
            }
        }

        return mapToString(masked);
    }

    /**
     * Collection의 민감정보를 마스킹합니다.
     */
    private static String maskCollection(Collection<?> collection) {
        List<Object> masked = new ArrayList<>();
        for (Object item : collection) {
            if (item instanceof Map) {
                masked.add(maskMap((Map<?, ?>) item));
            } else {
                masked.add(item);
            }
        }
        return masked.toString();
    }

    /**
     * 값을 부분적으로 마스킹합니다 (연락처, 이메일 등).
     *
     * 연락처 마스킹:
     * - 010-1234-5678 → 010-****-****
     * - 01012345678 → 010****5678 (중간 4자리 마스킹)
     *
     * 이메일 마스킹:
     * - john@example.com → jo**@example.com
     */
    private static String maskPartially(Object value) {
        if (value == null) {
            return "null";
        }

        String str = String.valueOf(value);

        // 이메일 마스킹
        if (str.contains("@")) {
            return maskEmail(str);
        }

        // 연락처 마스킹 (숫자만 있는 경우)
        if (str.matches("^\\d+$")) {
            return maskPhoneNumber(str);
        }

        // 하이픈 포함된 연락처 (010-1234-5678)
        if (str.matches("^\\d{3}-\\d{4}-\\d{4}$")) {
            return str.substring(0, 4) + "****-****";
        }

        // 기타: 뒤에서 4자리 마스킹
        if (str.length() <= PARTIAL_MASK_START) {
            return MASK;
        }
        int visibleLength = str.length() - PARTIAL_MASK_START;
        return str.substring(0, visibleLength) + MASK;
    }

    /**
     * 전화번호 마스킹 (숫자만 있는 경우)
     * 예: 01012345678 (11자리) → 010****5678 (중간 4자리 마스킹)
     */
    private static String maskPhoneNumber(String phoneNumber) {
        int length = phoneNumber.length();

        // 11자리 (010-1234-5678)
        if (length == 11) {
            return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7);
        }

        // 10자리 (031-123-4567 등)
        if (length == 10) {
            return phoneNumber.substring(0, 3) + "***" + phoneNumber.substring(6);
        }

        // 그 외: 중간 절반 마스킹
        if (length < 4) {
            return MASK;
        }

        int start = length / 4;
        int end = length - length / 4;
        return phoneNumber.substring(0, start) + "****" + phoneNumber.substring(end);
    }

    /**
     * 이메일 마스킹
     * 예: john@example.com → jo**@example.com
     */
    private static String maskEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return MASK;
        }

        String localPart = parts[0];
        String domain = parts[1];

        // 아이디 부분 마스킹
        if (localPart.length() <= 2) {
            return MASK + "@" + domain;
        }

        int visibleLength = Math.min(2, localPart.length() / 2);
        String masked = localPart.substring(0, visibleLength) + "**";

        return masked + "@" + domain;
    }

    /**
     * Map을 읽기 쉬운 문자열로 변환합니다.
     */
    private static String mapToString(Map<String, Object> map) {
        if (map.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        int count = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (count > 0) {
                sb.append(", ");
            }
            sb.append(entry.getKey())
                    .append("=")
                    .append(formatValue(entry.getValue()));
            count++;
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 값을 포맷팅합니다.
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return String.valueOf(value);
    }

    /**
     * 기본 타입 또는 Wrapper 클래스인지 확인합니다.
     */
    private static boolean isPrimitiveOrWrapper(Object obj) {
        return obj instanceof Number ||
                obj instanceof Boolean ||
                obj instanceof Character;
    }

    /**
     * 파라미터 배열을 마스킹된 문자열로 변환합니다.
     */
    public static String maskParameters(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(maskSensitiveData(args[i]));
        }

        sb.append("]");
        return sb.toString();
    }
}
