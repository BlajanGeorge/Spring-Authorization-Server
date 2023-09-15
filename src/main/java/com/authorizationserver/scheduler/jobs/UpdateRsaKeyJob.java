package com.authorizationserver.scheduler.jobs;

import com.authorizationserver.db.model.RsaKey;
import com.authorizationserver.db.repository.RsaKeyRepository;
import com.authorizationserver.model.RSADto;
import com.authorizationserver.util.CryptographyUtils;
import com.authorizationserver.exception.KeyGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Job to add new public and private keys
 *
 * @author Blajan George
 */
@Slf4j
@Component
public class UpdateRsaKeyJob implements Job {
    /**
     * Encryption secret
     */
    @Value("${encryptionSecret}")
    private String encryptionSecret;

    /**
     * Repository for database access and operations
     */
    private final RsaKeyRepository rsaKeyRepository;

    public UpdateRsaKeyJob(final RsaKeyRepository rsaKeyRepository) {
        this.rsaKeyRepository = rsaKeyRepository;
    }

    /**
     * Execute job instructions
     *
     * @param jobExecutionContext job context
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.info("Job {} start to execute fired by trigger {}. Current time: {}",
                jobExecutionContext.getJobDetail().getKey().getName(),
                jobExecutionContext.getTrigger().getKey().getName(),
                System.currentTimeMillis());

        try {
            RSADto rsaDto = CryptographyUtils.generateNewRsaKey();
            byte[] iv = CryptographyUtils.generateIv();
            rsaKeyRepository.saveAndFlush(new RsaKey(rsaDto.rsaKey().getKeyID(), rsaDto.rsaPublicKey().getEncoded(), CryptographyUtils.encrypt(rsaDto.rsaPrivateKey().getEncoded(), encryptionSecret, iv), iv, Instant.now()));
            log.info("New key registered in db.");
        } catch (Exception e) {
            log.error("Error encountered when generating a new key.", e);
            throw new KeyGenerationException(e.getMessage());
        }

    }
}
