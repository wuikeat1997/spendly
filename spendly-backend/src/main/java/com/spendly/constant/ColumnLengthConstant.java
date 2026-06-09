package com.spendly.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ColumnLengthConstant {

	public static final int DEFAULT_ID_LENGTH = 26;

	public static final int DEFAULT_USER_REF_NO_LENGTH = 32;

	public static final int DEFAULT_AUDIT_REF_LENGTH = DEFAULT_USER_REF_NO_LENGTH;

}
