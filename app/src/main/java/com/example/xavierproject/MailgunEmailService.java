package com.example.xavierproject;

import android.os.AsyncTask;
import android.util.Log;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class MailgunEmailService {

    private static final String TAG = "MailgunService";
    private static final String API_KEY = "d15695753de942060140ee9297ede702-42b8ce75-b2d36936";
    private static final String DOMAIN = "sandbox07ac5be83cc84ccea040cc810c89c6cf.mailgun.org";
    private static final String BASE_URL = "https://api.mailgun.net/v3/" + DOMAIN + "/messages";
    private static final String FROM_EMAIL = "Xavier City Support <postmaster@" + DOMAIN + ">";

    public interface EmailCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public static void sendStatusUpdateEmail(
            String userEmail,
            String userName,
            String complaintTitle,
            String complaintDescription,
            String location,
            String oldStatus,
            String newStatus,
            EmailCallback callback) {

        new SendEmailTask(callback).execute(
                userEmail,
                userName,
                complaintTitle,
                complaintDescription,
                location,
                oldStatus,
                newStatus
        );
    }

    private static class SendEmailTask extends AsyncTask<String, Void, EmailResult> {

        private EmailCallback callback;

        SendEmailTask(EmailCallback callback) {
            this.callback = callback;
        }

        @Override
        protected EmailResult doInBackground(String... params) {
            String userEmail = params[0];
            String userName = params[1];
            String complaintTitle = params[2];
            String complaintDescription = params[3];
            String location = params[4];
            String oldStatus = params[5];
            String newStatus = params[6];

            try {
                // Build email subject
                String subject = "Complaint Status Update - " + complaintTitle;

                // Build email body
                String emailBody = buildEmailBody(
                        userName,
                        complaintTitle,
                        complaintDescription,
                        location,
                        oldStatus,
                        newStatus
                );

                // Send email via Mailgun
                HttpResponse<JsonNode> response = Unirest.post(BASE_URL)
                        .basicAuth("api", API_KEY)
                        .queryString("from", FROM_EMAIL)
                        .queryString("to", userName + " <" + userEmail + ">")
                        .queryString("subject", subject)
                        .queryString("text", emailBody)
                        .queryString("html", buildHtmlEmailBody(
                                userName,
                                complaintTitle,
                                complaintDescription,
                                location,
                                oldStatus,
                                newStatus
                        ))
                        .asJson();

                if (response.getStatus() == 200) {
                    Log.d(TAG, "Email sent successfully to: " + userEmail);
                    return new EmailResult(true, "Email sent successfully");
                } else {
                    Log.e(TAG, "Failed to send email. Status: " + response.getStatus());
                    return new EmailResult(false, "Failed to send email. Status: " + response.getStatus());
                }

            } catch (UnirestException e) {
                Log.e(TAG, "Error sending email: " + e.getMessage());
                return new EmailResult(false, "Error: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(EmailResult result) {
            if (callback != null) {
                if (result.success) {
                    callback.onSuccess(result.message);
                } else {
                    callback.onFailure(result.message);
                }
            }
        }
    }

    private static String buildEmailBody(
            String userName,
            String complaintTitle,
            String complaintDescription,
            String location,
            String oldStatus,
            String newStatus) {

        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(userName).append(",\n\n");
        body.append("We are writing to inform you about an update on your complaint.\n\n");
        body.append("Complaint Details:\n");
        body.append("Title: ").append(complaintTitle).append("\n");
        body.append("Description: ").append(complaintDescription).append("\n");
        body.append("Location: ").append(location).append("\n\n");
        body.append("Status Update:\n");
        body.append("Previous Status: ").append(capitalizeFirst(oldStatus)).append("\n");
        body.append("New Status: ").append(capitalizeFirst(newStatus)).append("\n\n");
        body.append(getStatusMessage(newStatus)).append("\n\n");
        body.append("Thank you for your patience.\n\n");
        body.append("Best regards,\n");
        body.append("Xavier City Support Team");

        return body.toString();
    }

    private static String buildHtmlEmailBody(
            String userName,
            String complaintTitle,
            String complaintDescription,
            String location,
            String oldStatus,
            String newStatus) {

        String statusColor = getStatusColor(newStatus);

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { background-color: #f9f9f9; padding: 20px; border-radius: 0 0 5px 5px; }" +
                ".status-badge { display: inline-block; padding: 8px 16px; border-radius: 20px; font-weight: bold; color: white; background-color: " + statusColor + "; }" +
                ".detail-box { background-color: white; padding: 15px; margin: 10px 0; border-left: 4px solid #4CAF50; }" +
                ".footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h2>Complaint Status Update</h2>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hello <strong>" + userName + "</strong>,</p>" +
                "<p>We are writing to inform you about an update on your complaint.</p>" +
                "<div class='detail-box'>" +
                "<h3>Complaint Details</h3>" +
                "<p><strong>Title:</strong> " + complaintTitle + "</p>" +
                "<p><strong>Description:</strong> " + complaintDescription + "</p>" +
                "<p><strong>Location:</strong> " + location + "</p>" +
                "</div>" +
                "<div class='detail-box'>" +
                "<h3>Status Update</h3>" +
                "<p><strong>Previous Status:</strong> " + capitalizeFirst(oldStatus) + "</p>" +
                "<p><strong>New Status:</strong> <span class='status-badge'>" + capitalizeFirst(newStatus) + "</span></p>" +
                "</div>" +
                "<p>" + getStatusMessage(newStatus) + "</p>" +
                "<p>Thank you for your patience.</p>" +
                "<p><strong>Best regards,</strong><br>Xavier City Support Team</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>This is an automated message. Please do not reply to this email.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private static String getStatusMessage(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "Your complaint has been received and is pending review by our team.";
            case "acknowledged":
                return "Your complaint has been acknowledged by our team. We are reviewing the details and will take appropriate action soon.";
            case "ongoing":
                return "We are currently working on resolving your complaint. Our team is actively addressing the issue.";
            case "resolved":
                return "Great news! Your complaint has been resolved. Thank you for bringing this matter to our attention.";
            default:
                return "Your complaint status has been updated.";
        }
    }

    private static String getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "#FFA726"; // Orange
            case "acknowledged":
                return "#42A5F5"; // Blue
            case "ongoing":
                return "#FFEB3B"; // Yellow
            case "resolved":
                return "#66BB6A"; // Green
            default:
                return "#757575"; // Gray
        }
    }

    private static String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private static class EmailResult {
        boolean success;
        String message;

        EmailResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}