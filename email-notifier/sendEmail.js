const fs = require('fs');
const nodemailer = require('nodemailer');
const simpleGit = require('simple-git');
const git = simpleGit();

const customerRepoMapping = JSON.parse(fs.readFileSync('./customerMapping.json', 'utf8'));

async function main() {
    const log = await git.log(['-n', '2']);
    console.log('Recent commits:', log.all.map(c => `${c.hash} - ${c.message}`), log);
    if (log.all.length < 2) {
        console.log('Not enough commits to check for file differences.');
        return;
    }
    const targetCommit = log.all[1]; // Always use the second commit
    console.log(`Target commit: ${targetCommit.hash} - ${targetCommit.message}`);
    // Get the list of files changed (added/modified) in the target commit
    const diffTree = await git.raw([
        'diff-tree', '--no-commit-id', '--name-only', '-r', targetCommit.hash
    ]);
    const updatedFiles = diffTree.split('\n').filter(f => f.trim() !== '');
    console.log(`Files updated in the target commit: ${updatedFiles.join(', ')}`);

    // Assume repo name is from environment or set a default
    const repoName = 'github.com/ashish-mehta4/universal-connectors';

    customerRepoMapping.customers.forEach(customer => {
        customer.repositories.forEach(repo => {
            console.log(`Checking customer for repo: ${JSON.stringify(repo)}`);
            if (repo.repo === repoName) {
                const relevantUpdates = updatedFiles.filter(file => repo.files.includes(file));
                if (relevantUpdates.length > 0) {
                    console.log(`Sending notification to ${customer.email} for repo ${repoName}`);
                    sendNotification(customer.email, repoName, relevantUpdates);
                }
            }
        });
    });
}

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
        subject: `Universal Connector Repository Update Notification`,
        text: `The following files were updated in ${repoName}: ${files.join(', ')}`,
        html: `
            <div style="font-family: Arial, sans-serif;">
                <h2 style="color: #2d3748;">Universal Connector Notification</h2>
                <p><b>Repository:</b> <i>${repoName}</i></p>
                <p><b>The following files were updated in <i>GitHub</i>:</b></p>
                <ul>
                    ${files.map(file => `<li><b>${file}</b></li>`).join('')}
                </ul>
            </div>
        `
    };

    transporter.sendMail(mailOptions, (error, info) => {
        if (error) {
            console.error('Error sending email:', error);
        } else {
            console.log(`Email sent to ${email}:`, info.response);
        }
    });
}

main();