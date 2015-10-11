/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.service;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.mifosplatform.infrastructure.configuration.data.SMTPCredentialsData;
import org.mifosplatform.infrastructure.configuration.service.ExternalServicesPropertiesReadPlatformService;
import org.mifosplatform.infrastructure.core.domain.EmailDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GmailBackedPlatformEmailService implements PlatformEmailService {
	
	private final ExternalServicesPropertiesReadPlatformService externalServicesReadPlatformService;
	
	@Autowired
	public GmailBackedPlatformEmailService(final ExternalServicesPropertiesReadPlatformService externalServicesReadPlatformService){
		this.externalServicesReadPlatformService = externalServicesReadPlatformService;
	}

    @Override
    public void sendToUserAccount(final EmailDetail emailDetail, final String unencodedPassword) {
        final Email email = new SimpleEmail();
        final SMTPCredentialsData smtpCredentialsData = this.externalServicesReadPlatformService.getSMTPCredentials();
        final String authuserName = smtpCredentialsData.getUsername();

        final String authuser = smtpCredentialsData.getUsername();
        final String authpwd = smtpCredentialsData.getPassword();

        // Very Important, Don't use email.setAuthentication()
        email.setAuthenticator(new DefaultAuthenticator(authuser, authpwd));
        email.setDebug(false); // true if you want to debug
        email.setHostName(smtpCredentialsData.getHost());
        try {
        	if(smtpCredentialsData.isUseTLS()){
        		email.getMailSession().getProperties().put("mail.smtp.starttls.enable", "true");
        	}
        	email.setFrom(authuser, authuserName);

            final StringBuilder subjectBuilder = new StringBuilder().append("Decimus Financial: ").append(emailDetail.getContactName())
                    .append(" user account creation.");

            email.setSubject(subjectBuilder.toString());

            final String sendToEmail = emailDetail.getAddress();

            final StringBuilder messageBuilder = new StringBuilder().append("You are receiving this email as your email account: ")
                    .append(sendToEmail).append(" has being used to create a user account for an organisation named [")
                    .append(emailDetail.getOrganisationName()).append("] on Decimus Financial.")
                    .append("You can login using the following credentials: username: ").append(emailDetail.getUsername())
                    .append(" password: ").append(unencodedPassword);

            email.setMsg(messageBuilder.toString());

            email.addTo(sendToEmail, emailDetail.getContactName());
            email.send();
        } catch (final EmailException e) {
            throw new PlatformEmailSendException(e);
        }
    }
}