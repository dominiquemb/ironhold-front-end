package com.reqo.ironhold.web.api;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.interfaces.IMessageIndexService;
import com.reqo.ironhold.storage.interfaces.IMetaDataIndexService;
import com.reqo.ironhold.storage.interfaces.IMiscIndexService;
import com.reqo.ironhold.web.domain.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Created by ilya on 3/8/14.
 */
public abstract class AbstractController {



    public AbstractController() {

    }

}
