package net.atos.demo.service;

import net.atos.demo.domain.Wallet;
import net.atos.demo.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yiqiniu.easytrans.core.EasyTransFacade;
import com.yiqiniu.easytrans.demos.wallet.api.vo.WalletPayVO.*;

import java.util.Optional;

import javax.annotation.Resource;

/**
 * Service Implementation for managing {@link Wallet}.
 */
@Service
@Transactional
public class WalletService {

    private final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    
	@Resource
	private EasyTransFacade transaction;

	@Resource
	private JdbcTemplate jdbcTemplate;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    /**
     * Save a wallet.
     *
     * @param wallet the entity to save.
     * @return the persisted entity.
     */
    public Wallet save(Wallet wallet) {
        log.debug("Request to save Wallet : {}", wallet);
        return walletRepository.save(wallet);
    }

    /**
     * Get all the wallets.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Wallet> findAll(Pageable pageable) {
        log.debug("Request to get all Wallets");
        return walletRepository.findAll(pageable);
    }


    /**
     * Get one wallet by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Wallet> findOne(Long id) {
        log.debug("Request to get Wallet : {}", id);
        return walletRepository.findById(id);
    }

    /**
     * Delete the wallet by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Wallet : {}", id);
        walletRepository.deleteById(id);
    }
    
	@Transactional
	public WalletPayResponseVO doTryPay(WalletPayRequestVO param) {
		log.warn("exec doTryPay start...");
		int update = jdbcTemplate.update(
				"update biz_wallet set freeze_amount = freeze_amount + ? where user_id = ? and (total_amount - freeze_amount) >= ?;", 
				param.getPayAmount(), param.getUserId(), param.getPayAmount());
		
		if(update != 1){
			throw new RuntimeException("can not find specific user id or have not enought money");
		}
		
		WalletPayResponseVO walletPayTccMethodResult = new WalletPayResponseVO();
		walletPayTccMethodResult.setFreezeAmount(param.getPayAmount());
		log.warn("exec doTryPay end...");
		return walletPayTccMethodResult;
	}
	
	@Transactional
	public void doConfirmPay(WalletPayRequestVO param) {
		log.warn("exec doConfirmPay start...");
		int update = jdbcTemplate.update(
				"update biz_wallet set freeze_amount = freeze_amount - ?, total_amount = total_amount - ? where user_id = ?;", 
				param.getPayAmount(), param.getPayAmount(), param.getUserId());
		
		if(update != 1){
			throw new RuntimeException("thrown exception with the failed confirmPay, not match!");
		}
		
		if(param.getPayAmount()>=200 && (new java.util.Random().nextBoolean())) {
			throw new RuntimeException("thrown exception with the failed confirmPay, over 200!");
		}
		
		log.warn("exec doConfirmPay end...");
	}
	
	@Transactional
	public void doCancelPay(WalletPayRequestVO param) {
		log.warn("exec doCancelPay start...");
		int update = jdbcTemplate.update(
				"update biz_wallet set freeze_amount = freeze_amount - ? where user_id = ?;", 
				param.getPayAmount(),param.getUserId());
		
		if(update != 1){
			throw new RuntimeException("thrown exception with the failed cancelPay!");
		}
		log.warn("exec doCancelPay end...");
	}
}
