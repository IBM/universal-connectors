## Contributing In General
Our project welcomes external contributions. To contribute code or documentation, please submit a [pull request](https://github.com/IBM/logstash-filter-mongodb-guardium/pulls).

A good way to familiarize yourself with the codebase and contribution process is
to look for and tackle low-hanging fruit in the  [issue tracker][issues].

### Proposing new features

If you would like to implement a new feature, like a plug-in, utility or helper functions, please [raise an issue][issues] before sending a pull request so the feature can be discussed. This is to avoid
you wasting your valuable time working on a feature that the project developers
are not interested in accepting into the code base.

### Fixing bugs

If you would like to fix a bug, please [raise an issue][issues] before sending a
pull request so it can be tracked.

### Merge approval

The project maintainers use LGTM (Looks Good To Me) in comments on the code
review to indicate acceptance. A change requires LGTMs from two of the
maintainers of each component affected.

For a list of the maintainers, see the [MAINTAINERS.md](MAINTAINERS.md) page.


## Creating a pull request

If you just started to work on a new plug-in or a fix, please follow the [fork a repo][fork-a-repo] instructions by Github. However, if your code already exists elsewhere, like in a private repository, and you want to contribute it to here as well, the following steps are a variant on the [fork a repo][fork-a-repo] instructions, and may be handy for you as well: 

### Pushing existing code from another repository

1. Fork this public repository using the Fork button, go to the page of the fork repository you just created, and press the Code button: Copy the fork URL, and add it as another remote in your existing project (first time only):  
    ```
    git remote add myfork https://github.com/Tal-Daniel/universal-connectors.git
    git remote -v
    ```
2. Sync your repositories by running `git remote update` or `git fetch myfork`

3. Checkout the branch where your commits were made, like _master_, and find the commits that you added. If it's a range of sequential commits, mark only the first and last commits. For example: 
    
    ```
    git checkout master
    git log -- filter-plugin/pluginx
    ```

4. Checkout a branch that is based on your fork's main branch from step 1, above:
    
    ```
    git checkout -b myfork-main myfork/main
    git checkout -b myfork-main-pluginx
    ```

5. Cherry pick your commits you found, by running _git cherry-pick_. Add a _--no-commit_ flag (_-n_) if you need to edit the commit before hand, solve any conflicts. Make sure your commits are signed off, as described in the Legal section, below: 
    ```bash
    git cherry-pick -n 432fbbc3 54jj34fb 23jdb34 
    # A range of commits can also work if you are sure all commits belong: 
    # git cherry-pick -n 432fbbc3..348a4f34
    # edit some files, solve conflicts, or not, then
    git commit -s
    ```

6. That's it: The only thing left to do it push this branch to the remote forked repository, and create a pull request for it, from the browser: 
  
   ```
   git push -u myfork myfork-main-pluginx
   ```

_For IBM employees: To push to github.com under your personal github account, you will need to map your IBM account to your github account. Then, if you authenticate with an SSH key, you will need to edit your ~/.ssh/config to use your personal github account for any interaction with github.com._


## Legal

Each source file must include a license header for the Apache
Software License 2.0. Using the SPDX format is the simplest approach.
For example: 

```
/*
Copyright <holder> All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/
```

We have tried to make it as easy as possible to make contributions. This
applies to how we handle the legal aspects of contribution. We use the
same approach - the [Developer's Certificate of Origin 1.1 (DCO)][DCO] - that the Linux® Kernel [community](https://elinux.org/Developer_Certificate_Of_Origin)
uses to manage code contributions.

We simply ask that when submitting a patch for review, the developer
must include a sign-off statement in the commit message.

Here is an example Signed-off-by line, which indicates that the
submitter accepts the DCO:

```
Signed-off-by: John Doe <john.doe@example.com>
```

You can include this automatically when you commit a change to your
local git repository using the following command:

```
git commit -s
```

## Communication
Feel free to [create an issue][issues] to question or discuss anything with us. For more general questions regarding Guardium, we also refer you to the [IBM Security Guardium community][Guardium community].

## Testing

Coming soon ...

<!-- links -->
[issues]: https://github.com/IBM/universal-connectors/issues

[DCO]: https://developercertificate.org/

[Guardium community]: https://community.ibm.com/community/user/security/communities/community-home?communitykey=aa1a6549-4b51-421a-9c67-6dd41e65ef85&tab=groupdetails

[README.md]: ./README.md

[fork-a-repo]: https://docs.github.com/en/get-started/quickstart/fork-a-repo