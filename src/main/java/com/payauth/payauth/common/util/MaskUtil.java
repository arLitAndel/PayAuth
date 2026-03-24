package com.payauth.payauth.common.util;

public class MaskUtil {

    // 이름 마스킹
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) return "";

        if (name.length() == 1) return "*";
        if (name.length() == 2) return name.charAt(0) + "*";

        // 3글자 이상: 첫 글자와 마지막 글자만 노출
        return name.charAt(0)
                + "*".repeat(name.length() - 2)
                + name.charAt(name.length() - 1);
    }

    // 휴대폰번호 마스킹
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) return "";

        // 숫자만 추출
        String digits = phone.replaceAll("\\D", "");

        // 11자리: 010-XXXX-XXXX
        if (digits.length() == 11) {
            return digits.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-****-$3");
        }

        // 10자리: 010-XXX-XXXX
        if (digits.length() == 10) {
            return digits.replaceAll("(\\d{3})(\\d{3})(\\d{4})", "$1-***-$3");
        }

        return phone;
    }
}
