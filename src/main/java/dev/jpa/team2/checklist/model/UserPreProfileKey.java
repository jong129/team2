package dev.jpa.team2.checklist.model;

import java.util.Date;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "USER_PRE_PROFILE_KEY")
@SequenceGenerator(
    name = "SEQ_USER_PRE_PROFILE_KEY_GEN",
    sequenceName = "SEQ_USER_PRE_PROFILE_KEY_ID",
    allocationSize = 1
)
public class UserPreProfileKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_USER_PRE_PROFILE_KEY_GEN")
    @Column(name = "PROFILE_KEY_ID")
    private Long profileKeyId;

    @Column(name = "KEY_HASH", length = 64, nullable = false, unique = true)
    private String keyHash;

    @Lob
    @Column(name = "KEY_JSON", nullable = false)
    private String keyJson;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt = new Date();
}
