package com.authorizationserver.db.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entity for storing rsa keys
 *
 * @author Blajan George
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rsa_key")
public class RsaKey {
    @Id
    private String id;
    private byte[] publicKey;
    private byte[] privateKey;
    private byte[] iv;
    private Instant time;
}
