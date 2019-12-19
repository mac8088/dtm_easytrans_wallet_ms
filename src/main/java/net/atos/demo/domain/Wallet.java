package net.atos.demo.domain;
import io.swagger.annotations.ApiModel;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;

/**
 * Wallet Entity
 */
@ApiModel(description = "Wallet Entity")
@Entity
@Table(name = "biz_wallet")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Wallet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @NotNull
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @NotNull
    @Column(name = "freeze_amount", nullable = false)
    private Long freezeAmount;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    public Integer getUserId() {
        return userId;
    }

    public Wallet userId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public Wallet totalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getFreezeAmount() {
        return freezeAmount;
    }

    public Wallet freezeAmount(Long freezeAmount) {
        this.freezeAmount = freezeAmount;
        return this;
    }

    public void setFreezeAmount(Long freezeAmount) {
        this.freezeAmount = freezeAmount;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Wallet)) {
            return false;
        }
        return userId != null && userId.equals(((Wallet) o).userId);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Wallet{" +
            ", userId=" + getUserId() +
            ", totalAmount=" + getTotalAmount() +
            ", freezeAmount=" + getFreezeAmount() +
            "}";
    }
}
