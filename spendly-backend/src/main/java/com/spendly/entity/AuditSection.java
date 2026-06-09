package com.spendly.entity;

import com.spendly.annotation.GeneratedUlid;
import com.spendly.constant.ColumnLengthConstant;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class AuditSection implements Serializable {

	private static final String DEFAULT_AUDIT_REF_DEFINITION = "VARCHAR("
			+ ColumnLengthConstant.DEFAULT_AUDIT_REF_LENGTH + ") DEFAULT 'sys-admin'";

	@Id
	@GeneratedUlid
	@Column(updatable = false, nullable = false, length = ColumnLengthConstant.DEFAULT_ID_LENGTH)
	private String id;

	@Setter(AccessLevel.PRIVATE)
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false,
			columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
	private Instant createdAt = Instant.now();

	@Setter(AccessLevel.PRIVATE)
	@CreatedBy
	@Column(name = "created_by", nullable = false, updatable = false, columnDefinition = DEFAULT_AUDIT_REF_DEFINITION)
	private String createdBy;

	@Setter(AccessLevel.PRIVATE)
	@LastModifiedDate
	@Column(name = "last_modified_at", nullable = false,
			columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
	private Instant lastModifiedAt = Instant.now();

	@Setter(AccessLevel.PRIVATE)
	@LastModifiedBy
	@Column(name = "last_modified_by", nullable = false, columnDefinition = DEFAULT_AUDIT_REF_DEFINITION)
	private String lastModifiedBy;

}
