package webapp.mail

import webapp.models.AccountCredentials

interface MailService {
    fun sendEmail(
        to: String,
        subject: String,
        content: String,
        isMultipart: Boolean,
        isHtml: Boolean
    )

    fun sendEmailFromTemplate(
        account: AccountCredentials,
        templateName: String,
        titleKey: String
    )

    fun sendPasswordResetMail(accountCredentials: AccountCredentials)
    fun sendActivationEmail(accountCredentials: AccountCredentials)
    fun sendCreationEmail(accountCredentials: AccountCredentials)
}