package com.spendly.entity;

import com.spendly.constant.ColumnLengthConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser extends AuditSection {

	@Column(name = "email", nullable = false, unique = true, length = 320)
	private String email;

	@Column(name = "ref_no", nullable = false, unique = true, length = ColumnLengthConstant.DEFAULT_USER_REF_NO_LENGTH)
	private String refNo;

	public AppUser(String email, String refNo) {
		this.email = email;
		this.refNo = refNo;
	}

}
