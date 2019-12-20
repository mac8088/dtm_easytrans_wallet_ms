package net.atos.demo.service;

import java.util.Optional;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yiqiniu.easytrans.core.EasyTransFacade;
import com.yiqiniu.easytrans.protocol.BusinessProvider;
import com.yiqiniu.easytrans.protocol.tcc.EtTcc;

import com.yiqiniu.easytrans.demos.wallet.api.WalletPayMoneyService.WalletPayRequestVO;
import com.yiqiniu.easytrans.demos.wallet.api.WalletPayMoneyService.WalletPayResponseVO;
import com.yiqiniu.easytrans.demos.wallet.api.requestcfg.WalletPayRequestCfg;

import net.atos.demo.domain.Wallet;
import net.atos.demo.repository.WalletRepository;

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
    
	// 如果doTryPay的入参为集成了EasyTransRequest并带有BusinessIdentiffer的话则无需指定cfgClass
	@Transactional
	@EtTcc(confirmMethod = "doConfirmPay", cancelMethod = "doCancelPay", idempotentType = BusinessProvider.IDENPOTENT_TYPE_FRAMEWORK, cfgClass = WalletPayRequestCfg.class)
	public WalletPayResponseVO doTryPay(WalletPayRequestVO param) {
		log.warn("exec doTryPay start...");
		
		try {
			final Long userId = param.getUserId();
			log.warn("find wallet base on user: {} " + userId);
			
			//find wallet
			Optional<Wallet> opt = findOne(userId);
			if(!opt.isPresent()) {
				throw new Exception("without given user!"); 
			}
			
			Wallet wallet = opt.get();
			log.warn("found wallet : {} " + wallet);

			//validation
			long oldTotalVal = wallet.getTotalAmount();
			long oldFreeezeVal = wallet.getFreezeAmount();			
			if (oldTotalVal - oldFreeezeVal < param.getPayAmount()) {
				throw new Exception("without enough money!"); 
			}
			wallet.setFreezeAmount(oldFreeezeVal + param.getPayAmount());
			
			//update wallet
			save(wallet);	
			
		} catch(Exception ex) {
			throw new RuntimeException("can not find specific user id or have not enought money", ex);
		}
		
		WalletPayResponseVO walletPayTccMethodResult = new WalletPayResponseVO();
		walletPayTccMethodResult.setFreezeAmount(param.getPayAmount());
		log.warn("exec doTryPay end...");
		return walletPayTccMethodResult;
	}
	
	@Transactional
	public void doConfirmPay(WalletPayRequestVO param) {
		log.warn("exec doConfirmPay start...");
		
		try {
			final Long userId = param.getUserId();
			log.warn("find wallet base on user: {} " + userId);
			
			//find wallet
			Optional<Wallet> opt = findOne(userId);
			if(!opt.isPresent()) {
				throw new Exception("without given user!"); 
			}
			
			Wallet wallet = opt.get();
			log.warn("found wallet : {} " + wallet);
			
			long oldFreeezeVal = wallet.getFreezeAmount();
			wallet.setFreezeAmount(oldFreeezeVal - param.getPayAmount());
			log.warn("previous freeze_amount : {} new freeze_amount " + oldFreeezeVal, wallet.getFreezeAmount());
			
			long oldTotalValue = wallet.getTotalAmount();
			wallet.setTotalAmount(oldTotalValue - param.getPayAmount());
			log.warn("previous total_amount : {} new total_amount " + oldFreeezeVal, wallet.getFreezeAmount());
			
			//update wallet
			save(wallet);	
			
		}catch(Exception ex) {
			throw new RuntimeException("thrown exception with the failed confirmPay, not match!", ex);
		}
		
		if (param.getPayAmount() >= 200 && (new java.util.Random().nextBoolean())) {
			throw new RuntimeException("thrown exception with the failed confirmPay,  random check over 200!");
		}

		log.warn("exec doConfirmPay end...");
	}

	@Transactional
	public void doCancelPay(WalletPayRequestVO param) {
		log.warn("exec doCancelPay start...");
		try {
			final Long userId = param.getUserId();
			log.warn("find wallet base on user: {} " + userId);
			
			//find wallet
			Optional<Wallet> opt = findOne(userId);
			if(!opt.isPresent()) {
				throw new Exception("without given user!"); 
			}
			
			Wallet wallet = opt.get();
			log.warn("found wallet : {} " + wallet);

			long oldVal = wallet.getFreezeAmount();
			wallet.setFreezeAmount(oldVal - param.getPayAmount());
			log.warn("previous freeze_amount : {} new freeze_amount " + oldVal, wallet.getFreezeAmount());
			
			//update wallet
			save(wallet);			
		}catch(Exception ex) {
			throw new RuntimeException("thrown exception with the failed cancelPay!", ex);
		}

		log.warn("exec doCancelPay end...");
	}
}
