package com.cropkeeper.domain.farm.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Address {

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "street", nullable = false, length = 200)
    private String street;

    @Column(name = "detail", length = 100)
    private String detail;

    /**
     * 기존 Address를 기반으로 새로운 Address 생성 (부분 수정 지원)
     * null이 전달된 필드는 기존 값을 유지합니다.
     *
     * @param original 기존 Address (null 가능)
     * @param zipCode 새 우편번호 (null이면 기존 값 유지)
     * @param street 새 주소 (null이면 기존 값 유지)
     * @param detail 새 상세주소 (null이면 기존 값 유지)
     * @return 업데이트된 새 Address 객체
     */
    public static Address updateFrom(Address original, String zipCode, String street, String detail) {
        // 기존 Address가 없으면 모두 새 값 사용
        if (original == null) {
            return Address.builder()
                    .zipCode(zipCode)
                    .street(street)
                    .detail(detail)
                    .build();
        }

        // 기존 Address가 있으면 null이 아닌 값만 업데이트
        return Address.builder()
                .zipCode(zipCode != null ? zipCode : original.zipCode)
                .street(street != null ? street : original.street)
                .detail(detail != null ? detail : original.detail)
                .build();
    }
}
