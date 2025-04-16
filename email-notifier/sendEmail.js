const fs = require('fs');
const express = require('express');
const bodyParser = require('body-parser');
const nodemailer = require('nodemailer');

const app = express();
app.use(bodyParser.json());

const customerRepoMapping = JSON.parse(fs.readFileSync('./customerMapping.json', 'utf8'));

app.post('/webhook', (req, res) => {
    console.log('Received webhook:', req.body);
    const { repository, commits } = req.body;
    const repoName = repository.full_name; // e.g., "org/repo1"

    // Check if any monitored files are updated
    const updatedFiles = commits.flatMap(commit => commit.modified);

    customerRepoMapping.customers.forEach(customer => {
        customer.repositories.forEach(repo => {
            if (repo.repo === repoName) {
                const relevantUpdates = updatedFiles.filter(file => repo.files.includes(file));
                if (relevantUpdates.length > 0) {
                    console.log(`Sending notification to ${customer.email} for repo ${repoName}`);
                    sendNotification(customer.email, repoName, relevantUpdates);
                }
            }
        });
    });

    res.status(200).send('Webhook received');
});

function sendNotification(email, repoName, files) {
    const transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: {
            user: process.env.GMAIL_USER,
            pass: process.env.GMAIL_PASS,
        },
    });

    const mailOptions = {
        from: process.env.GMAIL_USER,
        to: email,
        subject: `Repository Update Notification: ${repoName}`,
        text: `The following files were updated in ${repoName}: ${files.join(', ')}`,
    };

    transporter.sendMail(mailOptions, (error, info) => {
        if (error) {
            console.error('Error sending email:', error);
        } else {
            console.log(`Email sent to ${email}:`, info.response);
        }
    });
}

app.listen(3000, () => {
    console.log('Webhook listener running on port 3000');
});