package uk.bovykina.matching_guru.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.bovykina.matching_guru.entity.Match;
import uk.bovykina.matching_guru.entity.User;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final SendGrid sendGrid;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.fromName:Notifier}")
    private String fromName;

    public void sendEmail(String to, String subject, String htmlContent) {
        Email fromEmail = new Email(fromAddress, fromName);
        Email toEmail = new Email(to);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(fromEmail, subject, toEmail, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            log.info("📧 Email queued -> {} (status {})", to, response.getStatusCode());
        } catch (IOException e) {
            log.error("❌ Error sending email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendAsync(String to, String subject, String htmlBody) {
        Mail mail = buildMail(to, subject, htmlBody);
        dispatch(mail);
    }

    public void sendMatchDeclinedEmail(Match match) {
        String mentorEmail = match.getMentor().getUser().getEmail();
        String menteeEmail = match.getMentee().getUser().getEmail();

        String subject = "❌ Match Declined";
        String content = "Unfortunately, your match has been declined by the coordinator. You are now unmatched and can be rematched.";

        sendEmail(mentorEmail, subject, content);
        sendEmail(menteeEmail, subject, content);
    }

    public void sendMatchCreatedEmail(Match match) {
        String mentorEmail = match.getMentor().getUser().getEmail();
        String menteeEmail = match.getMentee().getUser().getEmail();

        String subject = "🤝 New Match Created";
        String content = String.format(
                "Hi there!<br><br>A new match has been created:<br><br><strong>Mentor:</strong> %s %s<br><strong>Mentee:</strong> %s %s<br><br>You can now log in to view and manage this match.",
                match.getMentor().getUser().getFirstName(),
                match.getMentor().getUser().getLastName(),
                match.getMentee().getUser().getFirstName(),
                match.getMentee().getUser().getLastName()
        );

        sendEmail(mentorEmail, subject, content);
        sendEmail(menteeEmail, subject, content);
    }

    public void sendParticipantAcceptedEmail(Match match, Long userId) {
        User acceptingUser = userId.equals(match.getMentor().getUser().getId())
                ? match.getMentor().getUser()
                : match.getMentee().getUser();

        User otherUser = userId.equals(match.getMentor().getUser().getId())
                ? match.getMentee().getUser()
                : match.getMentor().getUser();

        String subject = "👍 Match Accepted";
        String content = String.format(
                "%s %s has accepted the match.<br>You're one step closer to a successful connection.",
                acceptingUser.getFirstName(), acceptingUser.getLastName()
        );

        sendEmail(otherUser.getEmail(), subject, content);
    }

    public void sendParticipantRejectedEmail(Match match, Long userId) {
        User rejectingUser = userId.equals(match.getMentor().getUser().getId())
                ? match.getMentor().getUser()
                : match.getMentee().getUser();

        User otherUser = userId.equals(match.getMentor().getUser().getId())
                ? match.getMentee().getUser()
                : match.getMentor().getUser();

        String subject = "❌ Match Rejected";
        String content = String.format(
                "Unfortunately, %s %s has rejected the match.",
                rejectingUser.getFirstName(), rejectingUser.getLastName()
        );

        sendEmail(otherUser.getEmail(), subject, content);
    }

    public void sendMatchApprovedEmail(Match match) {
        String mentorEmail = match.getMentor().getUser().getEmail();
        String menteeEmail = match.getMentee().getUser().getEmail();

        String subject = "✅ Match Approved!";
        String content = "Your match has been approved by a coordinator. You may now proceed to connect with your mentor/mentee.";

        sendEmail(mentorEmail, subject, content);
        sendEmail(menteeEmail, subject, content);
    }

    private Mail buildMail(String to, String subject, String htmlBody) {
        Email from = new Email(fromAddress, fromName);
        Email dest = new Email(to);
        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, subject, dest, content);
        mail.setReplyTo(from);
        return mail;
    }

    private void dispatch(Mail mail) {
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("📧 Email queued -> {} (status {})",
                        mail.getPersonalization().get(0).getTos().get(0).getEmail(),
                        response.getStatusCode());
            } else {
                log.error("SendGrid error {} – {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }
    }
}
