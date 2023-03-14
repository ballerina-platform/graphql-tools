# Represents AbortQueuedMigrationsInput
public type AbortQueuedMigrationsInput record {
    string? clientMutationId?;
    string ownerId?;
};

# Represents AcceptEnterpriseAdministratorInvitationInput
public type AcceptEnterpriseAdministratorInvitationInput record {
    string? clientMutationId?;
    string invitationId?;
};

# Represents AcceptTopicSuggestionInput
public type AcceptTopicSuggestionInput record {
    string? clientMutationId?;
    string repositoryId?;
    string name?;
};

# Represents AddAssigneesToAssignableInput
public type AddAssigneesToAssignableInput record {
    string? clientMutationId?;
    string assignableId?;
    string[] assigneeIds?;
};

# Represents AddCommentInput
public type AddCommentInput record {
    string? clientMutationId?;
    string body?;
    string subjectId?;
};

# Represents AddDiscussionCommentInput
public type AddDiscussionCommentInput record {
    string discussionId?;
    string? replyToId?;
    string? clientMutationId?;
    string body?;
};

# Represents AddEnterpriseSupportEntitlementInput
public type AddEnterpriseSupportEntitlementInput record {
    string? clientMutationId?;
    string enterpriseId?;
    string login?;
};

# Represents AddLabelsToLabelableInput
public type AddLabelsToLabelableInput record {
    string[] labelIds?;
    string? clientMutationId?;
    string labelableId?;
};

# Represents AddProjectCardInput
public type AddProjectCardInput record {
    string? note?;
    string? clientMutationId?;
    string? contentId?;
    string projectColumnId?;
};

# Represents AddProjectColumnInput
public type AddProjectColumnInput record {
    string? clientMutationId?;
    string name?;
    string projectId?;
};

# Represents AddProjectNextItemInput
public type AddProjectNextItemInput record {
    string? clientMutationId?;
    string contentId?;
    string projectId?;
};

# Represents AddPullRequestReviewCommentInput
public type AddPullRequestReviewCommentInput record {
    string? pullRequestReviewId?;
    string? path?;
    string? clientMutationId?;
    string? inReplyTo?;
    int? position?;
    string? pullRequestId?;
    string body?;
    anydata? commitOID?;
};

# Represents AddPullRequestReviewInput
public type AddPullRequestReviewInput record {
    DraftPullRequestReviewComment?[]? comments?;
    string? clientMutationId?;
    DraftPullRequestReviewThread?[]? threads?;
    string pullRequestId?;
    string? body?;
    string? event?;
    anydata? commitOID?;
};

# Represents AddPullRequestReviewThreadInput
public type AddPullRequestReviewThreadInput record {
    string path?;
    string? pullRequestReviewId?;
    string? side?;
    int line?;
    string? clientMutationId?;
    int? startLine?;
    string body?;
    string? pullRequestId?;
    string? startSide?;
};

# Represents AddReactionInput
public type AddReactionInput record {
    string? clientMutationId?;
    string subjectId?;
    string content?;
};

# Represents AddStarInput
public type AddStarInput record {
    string? clientMutationId?;
    string starrableId?;
};

# Represents AddUpvoteInput
public type AddUpvoteInput record {
    string? clientMutationId?;
    string subjectId?;
};

# Represents AddVerifiableDomainInput
public type AddVerifiableDomainInput record {
    string? clientMutationId?;
    anydata domain?;
    string ownerId?;
};

# Represents ApproveDeploymentsInput
public type ApproveDeploymentsInput record {
    string[] environmentIds?;
    string? clientMutationId?;
    string? comment?;
    string workflowRunId?;
};

# Represents ApproveVerifiableDomainInput
public type ApproveVerifiableDomainInput record {
    string? clientMutationId?;
    string id?;
};

# Represents ArchiveRepositoryInput
public type ArchiveRepositoryInput record {
    string? clientMutationId?;
    string repositoryId?;
};

# Represents AuditLogOrder
public type AuditLogOrder record {
    string? 'field?;
    string? direction?;
};

# Represents CancelEnterpriseAdminInvitationInput
public type CancelEnterpriseAdminInvitationInput record {
    string? clientMutationId?;
    string invitationId?;
};

# Represents CancelSponsorshipInput
public type CancelSponsorshipInput record {
    string? sponsorableLogin?;
    string? clientMutationId?;
    string? sponsorId?;
    string? sponsorLogin?;
    string? sponsorableId?;
};

# Represents ChangeUserStatusInput
public type ChangeUserStatusInput record {
    string? organizationId?;
    boolean? limitedAvailability?;
    string? emoji?;
    string? clientMutationId?;
    string? message?;
    anydata? expiresAt?;
};

# Represents CheckAnnotationData
public type CheckAnnotationData record {
    string path?;
    string? rawDetails?;
    string annotationLevel?;
    CheckAnnotationRange location?;
    string message?;
    string? title?;
};

# Represents CheckAnnotationRange
public type CheckAnnotationRange record {
    int endLine?;
    int? endColumn?;
    int? startColumn?;
    int startLine?;
};

# Represents CheckRunAction
public type CheckRunAction record {
    string identifier?;
    string description?;
    string label?;
};

# Represents CheckRunFilter
public type CheckRunFilter record {
    string? checkType?;
    int? appId?;
    string? checkName?;
    string? status?;
};

# Represents CheckRunOutput
public type CheckRunOutput record {
    string summary?;
    CheckRunOutputImage[]? images?;
    CheckAnnotationData[]? annotations?;
    string? text?;
    string title?;
};

# Represents CheckRunOutputImage
public type CheckRunOutputImage record {
    anydata imageUrl?;
    string alt?;
    string? caption?;
};

# Represents CheckSuiteAutoTriggerPreference
public type CheckSuiteAutoTriggerPreference record {
    string appId?;
    boolean setting?;
};

# Represents CheckSuiteFilter
public type CheckSuiteFilter record {
    int? appId?;
    string? checkName?;
};

# Represents ClearLabelsFromLabelableInput
public type ClearLabelsFromLabelableInput record {
    string? clientMutationId?;
    string labelableId?;
};

# Represents CloneProjectInput
public type CloneProjectInput record {
    string sourceId?;
    boolean? 'public?;
    boolean includeWorkflows?;
    string? clientMutationId?;
    string targetOwnerId?;
    string name?;
    string? body?;
};

# Represents CloneTemplateRepositoryInput
public type CloneTemplateRepositoryInput record {
    string visibility?;
    string? clientMutationId?;
    string repositoryId?;
    string name?;
    boolean? includeAllBranches?;
    string? description?;
    string ownerId?;
};

# Represents CloseIssueInput
public type CloseIssueInput record {
    string issueId?;
    string? clientMutationId?;
};

# Represents ClosePullRequestInput
public type ClosePullRequestInput record {
    string? clientMutationId?;
    string pullRequestId?;
};

# Represents CommitAuthor
public type CommitAuthor record {
    string[]? emails?;
    string? id?;
};

# Represents CommitContributionOrder
public type CommitContributionOrder record {
    string 'field?;
    string direction?;
};

# Represents CommitMessage
public type CommitMessage record {
    string? body?;
    string headline?;
};

# Represents CommittableBranch
public type CommittableBranch record {
    string? repositoryNameWithOwner?;
    string? branchName?;
    string? id?;
};

# Represents ContributionOrder
public type ContributionOrder record {
    string direction?;
};

# Represents ConvertProjectCardNoteToIssueInput
public type ConvertProjectCardNoteToIssueInput record {
    string? clientMutationId?;
    string repositoryId?;
    string projectCardId?;
    string? title?;
    string? body?;
};

# Represents ConvertPullRequestToDraftInput
public type ConvertPullRequestToDraftInput record {
    string? clientMutationId?;
    string pullRequestId?;
};

# Represents CreateBranchProtectionRuleInput
public type CreateBranchProtectionRuleInput record {
    boolean? restrictsReviewDismissals?;
    boolean? restrictsPushes?;
    string? clientMutationId?;
    string[]? bypassPullRequestActorIds?;
    string pattern?;
    string[]? reviewDismissalActorIds?;
    boolean? isAdminEnforced?;
    boolean? dismissesStaleReviews?;
    boolean? allowsForcePushes?;
    string[]? bypassForcePushActorIds?;
    boolean? requiresApprovingReviews?;
    boolean? requiresStatusChecks?;
    boolean? requiresCodeOwnerReviews?;
    RequiredStatusCheckInput[]? requiredStatusChecks?;
    string repositoryId?;
    int? requiredApprovingReviewCount?;
    boolean? requiresLinearHistory?;
    boolean? allowsDeletions?;
    string[]? pushActorIds?;
    string[]? requiredStatusCheckContexts?;
    boolean? requiresCommitSignatures?;
    boolean? requiresStrictStatusChecks?;
    boolean? requiresConversationResolution?;
};

# Represents CreateCheckRunInput
public type CreateCheckRunInput record {
    string? conclusion?;
    CheckRunOutput? output?;
    anydata? completedAt?;
    anydata? detailsUrl?;
    string? clientMutationId?;
    string repositoryId?;
    string name?;
    string? externalId?;
    anydata? startedAt?;
    anydata headSha?;
    CheckRunAction[]? actions?;
    string? status?;
};

# Represents CreateCheckSuiteInput
public type CreateCheckSuiteInput record {
    string? clientMutationId?;
    string repositoryId?;
    anydata headSha?;
};

# Represents CreateCommitOnBranchInput
public type CreateCommitOnBranchInput record {
    string? clientMutationId?;
    CommitMessage message?;
    CommittableBranch branch?;
    FileChanges? fileChanges?;
    anydata expectedHeadOid?;
};

# Represents CreateDiscussionInput
public type CreateDiscussionInput record {
    string? clientMutationId?;
    string repositoryId?;
    string title?;
    string body?;
    string categoryId?;
};

# Represents CreateEnterpriseOrganizationInput
public type CreateEnterpriseOrganizationInput record {
    string profileName?;
    string billingEmail?;
    string? clientMutationId?;
    string[] adminLogins?;
    string enterpriseId?;
    string login?;
};

# Represents CreateEnvironmentInput
public type CreateEnvironmentInput record {
    string? clientMutationId?;
    string repositoryId?;
    string name?;
};

# Represents CreateIpAllowListEntryInput
public type CreateIpAllowListEntryInput record {
    string? clientMutationId?;
    string? name?;
    string allowListValue?;
    string ownerId?;
    boolean isActive?;
};

# Represents CreateIssueInput
public type CreateIssueInput record {
    string[]? labelIds?;
    string? clientMutationId?;
    string repositoryId?;
    string? milestoneId?;
    string[]? projectIds?;
    string title?;
    string? body?;
    string[]? assigneeIds?;
    string? issueTemplate?;
};

# Represents CreateMigrationSourceInput
public type CreateMigrationSourceInput record {
    string? clientMutationId?;
    string name?;
    string accessToken?;
    string ownerId?;
    string 'type?;
    string? githubPat?;
    string url?;
};

# Represents CreateProjectInput
public type CreateProjectInput record {
    string? template?;
    string[]? repositoryIds?;
    string? clientMutationId?;
    string name?;
    string ownerId?;
    string? body?;
};

# Represents CreatePullRequestInput
public type CreatePullRequestInput record {
    string baseRefName?;
    string? clientMutationId?;
    boolean? draft?;
    string repositoryId?;
    boolean? maintainerCanModify?;
    string title?;
    string? body?;
    string headRefName?;
};

# Represents CreateRefInput
public type CreateRefInput record {
    string? clientMutationId?;
    string repositoryId?;
    string name?;
    anydata oid?;
};

# Represents CreateRepositoryInput
public type CreateRepositoryInput record {
    boolean? template?;
    boolean? hasIssuesEnabled?;
    string visibility?;
    anydata? homepageUrl?;
    boolean? hasWikiEnabled?;
    string? clientMutationId?;
    string? teamId?;
    string name?;
    string? description?;
    string? ownerId?;
};

# Represents CreateSponsorshipInput
public type CreateSponsorshipInput record {
    int? amount?;
    string? privacyLevel?;
    string? sponsorableLogin?;
    string? tierId?;
    string? clientMutationId?;
    string? sponsorId?;
    boolean? isRecurring?;
    boolean? receiveEmails?;
    string? sponsorLogin?;
    string? sponsorableId?;
};

# Represents CreateTeamDiscussionCommentInput
public type CreateTeamDiscussionCommentInput record {
    string discussionId?;
    string? clientMutationId?;
    string body?;
};

# Represents CreateTeamDiscussionInput
public type CreateTeamDiscussionInput record {
    boolean? 'private?;
    string? clientMutationId?;
    string teamId?;
    string title?;
    string body?;
};

# Represents DeclineTopicSuggestionInput
public type DeclineTopicSuggestionInput record {
    string reason?;
    string? clientMutationId?;
    string repositoryId?;
    string name?;
};

# Represents DeleteBranchProtectionRuleInput
public type DeleteBranchProtectionRuleInput record {
    string? clientMutationId?;
    string branchProtectionRuleId?;
};

# Represents DeleteDeploymentInput
public type DeleteDeploymentInput record {
    string? clientMutationId?;
    string id?;
};

# Represents DeleteDiscussionCommentInput
public type DeleteDiscussionCommentInput record {
    string? clientMutationId?;
    string id?;
};

# Represents DeleteDiscussionInput
public type DeleteDiscussionInput record {
    string? clientMutationId?;
    string id?;
};

# Represents DeleteEnvironmentInput
public type DeleteEnvironmentInput record {
    string? clientMutationId?;
    string id?;
};

# Represents DeleteIpAllowListEntryInput
public type DeleteIpAllowListEntryInput record {
    string ipAllowListEntryId?;
    string? clientMutationId?;
};

# Represents DeleteIssueCommentInput
public type DeleteIssueCommentInput record {
    string? clientMutationId?;
    string id?;
};

# Represents DeleteIssueInput
public type DeleteIssueInput record {
    string issueId?;
    string? clientMutationId?;
};

# Represents DeleteProjectCardInput
public type DeleteProjectCardInput record {
    string? clientMutationId?;
    string cardId?;
};

# Represents DeleteProjectColumnInput
public type DeleteProjectColumnInput record {
    string columnId?;
    string? clientMutationId?;
};

# Represents DeleteProjectInput
public type DeleteProjectInput record {
    string? clientMutationId?;
    string projectId?;
};

# Represents DeleteProjectNextItemInput
public type DeleteProjectNextItemInput record {
    string itemId?;
    string? clientMutationId?;
    string projectId?;
};

# Represents DeletePullRequestReviewCommentInput
public type DeletePullRequestReviewCommentInput record {
    string? clientMutationId?;
    string id?;
};

# Represents DeletePullRequestReviewInput
public type DeletePullRequestReviewInput record {
    string pullRequestReviewId?;
    string? clientMutationId?;
};

# Represents DeleteRefInput
public type DeleteRefInput record {
    string? clientMutationId?;
    string refId?;
};

# Represents DeleteTeamDiscussionCommentInput
public type DeleteTeamDiscussionCommentInput record {
    string? clientMutationId?;
    string id?;
};

# Represents DeleteTeamDiscussionInput
public type DeleteTeamDiscussionInput record {
    string? clientMutationId?;
    string id?;
};

# Represents DeleteVerifiableDomainInput
public type DeleteVerifiableDomainInput record {
    string? clientMutationId?;
    string id?;
};

# Represents DeploymentOrder
public type DeploymentOrder record {
    string 'field?;
    string direction?;
};

# Represents DisablePullRequestAutoMergeInput
public type DisablePullRequestAutoMergeInput record {
    string? clientMutationId?;
    string pullRequestId?;
};

# Represents DiscussionOrder
public type DiscussionOrder record {
    string 'field?;
    string direction?;
};

# Represents DismissPullRequestReviewInput
public type DismissPullRequestReviewInput record {
    string pullRequestReviewId?;
    string? clientMutationId?;
    string message?;
};

# Represents DismissRepositoryVulnerabilityAlertInput
public type DismissRepositoryVulnerabilityAlertInput record {
    string dismissReason?;
    string? clientMutationId?;
    string repositoryVulnerabilityAlertId?;
};

# Represents DraftPullRequestReviewComment
public type DraftPullRequestReviewComment record {
    string path?;
    int position?;
    string body?;
};

# Represents DraftPullRequestReviewThread
public type DraftPullRequestReviewThread record {
    string path?;
    string? side?;
    int line?;
    int? startLine?;
    string? startSide?;
    string body?;
};

# Represents EnablePullRequestAutoMergeInput
public type EnablePullRequestAutoMergeInput record {
    string? commitBody?;
    string? authorEmail?;
    string? mergeMethod?;
    string? commitHeadline?;
    string? clientMutationId?;
    string pullRequestId?;
};

# Represents EnterpriseAdministratorInvitationOrder
public type EnterpriseAdministratorInvitationOrder record {
    string 'field?;
    string direction?;
};

# Represents EnterpriseMemberOrder
public type EnterpriseMemberOrder record {
    string 'field?;
    string direction?;
};

# Represents EnterpriseServerInstallationOrder
public type EnterpriseServerInstallationOrder record {
    string 'field?;
    string direction?;
};

# Represents EnterpriseServerUserAccountEmailOrder
public type EnterpriseServerUserAccountEmailOrder record {
    string 'field?;
    string direction?;
};

# Represents EnterpriseServerUserAccountOrder
public type EnterpriseServerUserAccountOrder record {
    string 'field?;
    string direction?;
};

# Represents EnterpriseServerUserAccountsUploadOrder
public type EnterpriseServerUserAccountsUploadOrder record {
    string 'field?;
    string direction?;
};

# Represents FileAddition
public type FileAddition record {
    string path?;
    anydata contents?;
};

# Represents FileChanges
public type FileChanges record {
    FileAddition[]? additions?;
    FileDeletion[]? deletions?;
};

# Represents FileDeletion
public type FileDeletion record {
    string path?;
};

# Represents FollowUserInput
public type FollowUserInput record {
    string? clientMutationId?;
    string userId?;
};

# Represents GistOrder
public type GistOrder record {
    string 'field?;
    string direction?;
};

# Represents GrantEnterpriseOrganizationsMigratorRoleInput
public type GrantEnterpriseOrganizationsMigratorRoleInput record {
    string? clientMutationId?;
    string enterpriseId?;
    string login?;
};

# Represents GrantMigratorRoleInput
public type GrantMigratorRoleInput record {
    string organizationId?;
    string actor?;
    string actorType?;
    string? clientMutationId?;
};

# Represents InviteEnterpriseAdminInput
public type InviteEnterpriseAdminInput record {
    string? role?;
    string? clientMutationId?;
    string enterpriseId?;
    string? email?;
    string? invitee?;
};

# Represents IpAllowListEntryOrder
public type IpAllowListEntryOrder record {
    string 'field?;
    string direction?;
};

# Represents IssueCommentOrder
public type IssueCommentOrder record {
    string 'field?;
    string direction?;
};

# Represents IssueFilters
public type IssueFilters record {
    boolean? viewerSubscribed?;
    string? milestone?;
    string? createdBy?;
    string? assignee?;
    string? mentioned?;
    string? milestoneNumber?;
    string[]? labels?;
    anydata? since?;
    string[]? states?;
};

# Represents IssueOrder
public type IssueOrder record {
    string 'field?;
    string direction?;
};

# Represents LabelOrder
public type LabelOrder record {
    string 'field?;
    string direction?;
};

# Represents LanguageOrder
public type LanguageOrder record {
    string 'field?;
    string direction?;
};

# Represents LinkRepositoryToProjectInput
public type LinkRepositoryToProjectInput record {
    string? clientMutationId?;
    string repositoryId?;
    string projectId?;
};

# Represents LockLockableInput
public type LockLockableInput record {
    string lockableId?;
    string? clientMutationId?;
    string? lockReason?;
};

# Represents MarkDiscussionCommentAsAnswerInput
public type MarkDiscussionCommentAsAnswerInput record {
    string? clientMutationId?;
    string id?;
};

# Represents MarkFileAsViewedInput
public type MarkFileAsViewedInput record {
    string path?;
    string? clientMutationId?;
    string pullRequestId?;
};

# Represents MarkPullRequestReadyForReviewInput
public type MarkPullRequestReadyForReviewInput record {
    string? clientMutationId?;
    string pullRequestId?;
};

# Represents MergeBranchInput
public type MergeBranchInput record {
    string head?;
    string? authorEmail?;
    string? commitMessage?;
    string? clientMutationId?;
    string repositoryId?;
    string base?;
};

# Represents MergePullRequestInput
public type MergePullRequestInput record {
    string? commitBody?;
    string? authorEmail?;
    string? mergeMethod?;
    string? commitHeadline?;
    string? clientMutationId?;
    string pullRequestId?;
    anydata? expectedHeadOid?;
};

# Represents MilestoneOrder
public type MilestoneOrder record {
    string 'field?;
    string direction?;
};

# Represents MinimizeCommentInput
public type MinimizeCommentInput record {
    string? clientMutationId?;
    string classifier?;
    string subjectId?;
};

# Represents MoveProjectCardInput
public type MoveProjectCardInput record {
    string columnId?;
    string? clientMutationId?;
    string cardId?;
    string? afterCardId?;
};

# Represents MoveProjectColumnInput
public type MoveProjectColumnInput record {
    string columnId?;
    string? clientMutationId?;
    string? afterColumnId?;
};

# Represents OrgEnterpriseOwnerOrder
public type OrgEnterpriseOwnerOrder record {
    string 'field?;
    string direction?;
};

# Represents OrganizationOrder
public type OrganizationOrder record {
    string 'field?;
    string direction?;
};

# Represents PackageFileOrder
public type PackageFileOrder record {
    string? 'field?;
    string? direction?;
};

# Represents PackageOrder
public type PackageOrder record {
    string? 'field?;
    string? direction?;
};

# Represents PackageVersionOrder
public type PackageVersionOrder record {
    string? 'field?;
    string? direction?;
};

# Represents PinIssueInput
public type PinIssueInput record {
    string issueId?;
    string? clientMutationId?;
};

# Represents ProjectOrder
public type ProjectOrder record {
    string 'field?;
    string direction?;
};

# Represents PullRequestOrder
public type PullRequestOrder record {
    string 'field?;
    string direction?;
};

# Represents ReactionOrder
public type ReactionOrder record {
    string 'field?;
    string direction?;
};

# Represents RefOrder
public type RefOrder record {
    string 'field?;
    string direction?;
};

# Represents RegenerateEnterpriseIdentityProviderRecoveryCodesInput
public type RegenerateEnterpriseIdentityProviderRecoveryCodesInput record {
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents RegenerateVerifiableDomainTokenInput
public type RegenerateVerifiableDomainTokenInput record {
    string? clientMutationId?;
    string id?;
};

# Represents RejectDeploymentsInput
public type RejectDeploymentsInput record {
    string[] environmentIds?;
    string? clientMutationId?;
    string? comment?;
    string workflowRunId?;
};

# Represents ReleaseOrder
public type ReleaseOrder record {
    string 'field?;
    string direction?;
};

# Represents RemoveAssigneesFromAssignableInput
public type RemoveAssigneesFromAssignableInput record {
    string? clientMutationId?;
    string assignableId?;
    string[] assigneeIds?;
};

# Represents RemoveEnterpriseAdminInput
public type RemoveEnterpriseAdminInput record {
    string? clientMutationId?;
    string enterpriseId?;
    string login?;
};

# Represents RemoveEnterpriseIdentityProviderInput
public type RemoveEnterpriseIdentityProviderInput record {
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents RemoveEnterpriseOrganizationInput
public type RemoveEnterpriseOrganizationInput record {
    string organizationId?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents RemoveEnterpriseSupportEntitlementInput
public type RemoveEnterpriseSupportEntitlementInput record {
    string? clientMutationId?;
    string enterpriseId?;
    string login?;
};

# Represents RemoveLabelsFromLabelableInput
public type RemoveLabelsFromLabelableInput record {
    string[] labelIds?;
    string? clientMutationId?;
    string labelableId?;
};

# Represents RemoveOutsideCollaboratorInput
public type RemoveOutsideCollaboratorInput record {
    string organizationId?;
    string? clientMutationId?;
    string userId?;
};

# Represents RemoveReactionInput
public type RemoveReactionInput record {
    string? clientMutationId?;
    string subjectId?;
    string content?;
};

# Represents RemoveStarInput
public type RemoveStarInput record {
    string? clientMutationId?;
    string starrableId?;
};

# Represents RemoveUpvoteInput
public type RemoveUpvoteInput record {
    string? clientMutationId?;
    string subjectId?;
};

# Represents ReopenIssueInput
public type ReopenIssueInput record {
    string issueId?;
    string? clientMutationId?;
};

# Represents ReopenPullRequestInput
public type ReopenPullRequestInput record {
    string? clientMutationId?;
    string pullRequestId?;
};

# Represents RepositoryInvitationOrder
public type RepositoryInvitationOrder record {
    string 'field?;
    string direction?;
};

# Represents RepositoryMigrationOrder
public type RepositoryMigrationOrder record {
    string 'field?;
    string direction?;
};

# Represents RepositoryOrder
public type RepositoryOrder record {
    string 'field?;
    string direction?;
};

# Represents RequestReviewsInput
public type RequestReviewsInput record {
    string? clientMutationId?;
    string[]? userIds?;
    boolean? union?;
    string pullRequestId?;
    string[]? teamIds?;
};

# Represents RequiredStatusCheckInput
public type RequiredStatusCheckInput record {
    string? appId?;
    string context?;
};

# Represents RerequestCheckSuiteInput
public type RerequestCheckSuiteInput record {
    string? clientMutationId?;
    string repositoryId?;
    string checkSuiteId?;
};

# Represents ResolveReviewThreadInput
public type ResolveReviewThreadInput record {
    string threadId?;
    string? clientMutationId?;
};

# Represents RevokeEnterpriseOrganizationsMigratorRoleInput
public type RevokeEnterpriseOrganizationsMigratorRoleInput record {
    string? clientMutationId?;
    string enterpriseId?;
    string login?;
};

# Represents RevokeMigratorRoleInput
public type RevokeMigratorRoleInput record {
    string organizationId?;
    string actor?;
    string actorType?;
    string? clientMutationId?;
};

# Represents SavedReplyOrder
public type SavedReplyOrder record {
    string 'field?;
    string direction?;
};

# Represents SecurityAdvisoryIdentifierFilter
public type SecurityAdvisoryIdentifierFilter record {
    string 'type?;
    string value?;
};

# Represents SecurityAdvisoryOrder
public type SecurityAdvisoryOrder record {
    string 'field?;
    string direction?;
};

# Represents SecurityVulnerabilityOrder
public type SecurityVulnerabilityOrder record {
    string 'field?;
    string direction?;
};

# Represents SetEnterpriseIdentityProviderInput
public type SetEnterpriseIdentityProviderInput record {
    anydata ssoUrl?;
    string idpCertificate?;
    string? clientMutationId?;
    string digestMethod?;
    string signatureMethod?;
    string enterpriseId?;
    string? issuer?;
};

# Represents SetOrganizationInteractionLimitInput
public type SetOrganizationInteractionLimitInput record {
    string organizationId?;
    string 'limit?;
    string? clientMutationId?;
    string? expiry?;
};

# Represents SetRepositoryInteractionLimitInput
public type SetRepositoryInteractionLimitInput record {
    string 'limit?;
    string? clientMutationId?;
    string repositoryId?;
    string? expiry?;
};

# Represents SetUserInteractionLimitInput
public type SetUserInteractionLimitInput record {
    string 'limit?;
    string? clientMutationId?;
    string? expiry?;
    string userId?;
};

# Represents SponsorOrder
public type SponsorOrder record {
    string 'field?;
    string direction?;
};

# Represents SponsorableOrder
public type SponsorableOrder record {
    string 'field?;
    string direction?;
};

# Represents SponsorsActivityOrder
public type SponsorsActivityOrder record {
    string 'field?;
    string direction?;
};

# Represents SponsorsTierOrder
public type SponsorsTierOrder record {
    string 'field?;
    string direction?;
};

# Represents SponsorshipNewsletterOrder
public type SponsorshipNewsletterOrder record {
    string 'field?;
    string direction?;
};

# Represents SponsorshipOrder
public type SponsorshipOrder record {
    string 'field?;
    string direction?;
};

# Represents StarOrder
public type StarOrder record {
    string 'field?;
    string direction?;
};

# Represents StartRepositoryMigrationInput
public type StartRepositoryMigrationInput record {
    string sourceId?;
    string? gitArchiveUrl?;
    string? clientMutationId?;
    string ownerId?;
    anydata sourceRepositoryUrl?;
    string repositoryName?;
    boolean? continueOnError?;
    string? metadataArchiveUrl?;
};

# Represents SubmitPullRequestReviewInput
public type SubmitPullRequestReviewInput record {
    string? pullRequestReviewId?;
    string? clientMutationId?;
    string? pullRequestId?;
    string event?;
    string? body?;
};

# Represents TeamDiscussionCommentOrder
public type TeamDiscussionCommentOrder record {
    string 'field?;
    string direction?;
};

# Represents TeamDiscussionOrder
public type TeamDiscussionOrder record {
    string 'field?;
    string direction?;
};

# Represents TeamMemberOrder
public type TeamMemberOrder record {
    string 'field?;
    string direction?;
};

# Represents TeamOrder
public type TeamOrder record {
    string 'field?;
    string direction?;
};

# Represents TeamRepositoryOrder
public type TeamRepositoryOrder record {
    string 'field?;
    string direction?;
};

# Represents TransferIssueInput
public type TransferIssueInput record {
    string issueId?;
    string? clientMutationId?;
    string repositoryId?;
};

# Represents UnarchiveRepositoryInput
public type UnarchiveRepositoryInput record {
    string? clientMutationId?;
    string repositoryId?;
};

# Represents UnfollowUserInput
public type UnfollowUserInput record {
    string? clientMutationId?;
    string userId?;
};

# Represents UnlinkRepositoryFromProjectInput
public type UnlinkRepositoryFromProjectInput record {
    string? clientMutationId?;
    string repositoryId?;
    string projectId?;
};

# Represents UnlockLockableInput
public type UnlockLockableInput record {
    string lockableId?;
    string? clientMutationId?;
};

# Represents UnmarkDiscussionCommentAsAnswerInput
public type UnmarkDiscussionCommentAsAnswerInput record {
    string? clientMutationId?;
    string id?;
};

# Represents UnmarkFileAsViewedInput
public type UnmarkFileAsViewedInput record {
    string path?;
    string? clientMutationId?;
    string pullRequestId?;
};

# Represents UnmarkIssueAsDuplicateInput
public type UnmarkIssueAsDuplicateInput record {
    string canonicalId?;
    string? clientMutationId?;
    string duplicateId?;
};

# Represents UnminimizeCommentInput
public type UnminimizeCommentInput record {
    string? clientMutationId?;
    string subjectId?;
};

# Represents UnpinIssueInput
public type UnpinIssueInput record {
    string issueId?;
    string? clientMutationId?;
};

# Represents UnresolveReviewThreadInput
public type UnresolveReviewThreadInput record {
    string threadId?;
    string? clientMutationId?;
};

# Represents UpdateBranchProtectionRuleInput
public type UpdateBranchProtectionRuleInput record {
    boolean? restrictsReviewDismissals?;
    boolean? restrictsPushes?;
    string? clientMutationId?;
    string[]? bypassPullRequestActorIds?;
    string? pattern?;
    string[]? reviewDismissalActorIds?;
    boolean? isAdminEnforced?;
    boolean? dismissesStaleReviews?;
    string branchProtectionRuleId?;
    boolean? allowsForcePushes?;
    string[]? bypassForcePushActorIds?;
    boolean? requiresApprovingReviews?;
    boolean? requiresStatusChecks?;
    boolean? requiresCodeOwnerReviews?;
    RequiredStatusCheckInput[]? requiredStatusChecks?;
    int? requiredApprovingReviewCount?;
    boolean? requiresLinearHistory?;
    boolean? allowsDeletions?;
    string[]? pushActorIds?;
    string[]? requiredStatusCheckContexts?;
    boolean? requiresCommitSignatures?;
    boolean? requiresStrictStatusChecks?;
    boolean? requiresConversationResolution?;
};

# Represents UpdateCheckRunInput
public type UpdateCheckRunInput record {
    string? conclusion?;
    CheckRunOutput? output?;
    anydata? completedAt?;
    anydata? detailsUrl?;
    string? clientMutationId?;
    string repositoryId?;
    string? name?;
    string? externalId?;
    anydata? startedAt?;
    string checkRunId?;
    CheckRunAction[]? actions?;
    string? status?;
};

# Represents UpdateCheckSuitePreferencesInput
public type UpdateCheckSuitePreferencesInput record {
    string? clientMutationId?;
    string repositoryId?;
    CheckSuiteAutoTriggerPreference[] autoTriggerPreferences?;
};

# Represents UpdateDiscussionCommentInput
public type UpdateDiscussionCommentInput record {
    string? clientMutationId?;
    string commentId?;
    string body?;
};

# Represents UpdateDiscussionInput
public type UpdateDiscussionInput record {
    string discussionId?;
    string? clientMutationId?;
    string? title?;
    string? body?;
    string? categoryId?;
};

# Represents UpdateEnterpriseAdministratorRoleInput
public type UpdateEnterpriseAdministratorRoleInput record {
    string role?;
    string? clientMutationId?;
    string enterpriseId?;
    string login?;
};

# Represents UpdateEnterpriseAllowPrivateRepositoryForkingSettingInput
public type UpdateEnterpriseAllowPrivateRepositoryForkingSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseDefaultRepositoryPermissionSettingInput
public type UpdateEnterpriseDefaultRepositoryPermissionSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseMembersCanChangeRepositoryVisibilitySettingInput
public type UpdateEnterpriseMembersCanChangeRepositoryVisibilitySettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseMembersCanCreateRepositoriesSettingInput
public type UpdateEnterpriseMembersCanCreateRepositoriesSettingInput record {
    boolean? membersCanCreatePrivateRepositories?;
    boolean? membersCanCreatePublicRepositories?;
    boolean? membersCanCreateInternalRepositories?;
    string? settingValue?;
    boolean? membersCanCreateRepositoriesPolicyEnabled?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseMembersCanDeleteIssuesSettingInput
public type UpdateEnterpriseMembersCanDeleteIssuesSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseMembersCanDeleteRepositoriesSettingInput
public type UpdateEnterpriseMembersCanDeleteRepositoriesSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseMembersCanInviteCollaboratorsSettingInput
public type UpdateEnterpriseMembersCanInviteCollaboratorsSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseMembersCanMakePurchasesSettingInput
public type UpdateEnterpriseMembersCanMakePurchasesSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseMembersCanUpdateProtectedBranchesSettingInput
public type UpdateEnterpriseMembersCanUpdateProtectedBranchesSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseMembersCanViewDependencyInsightsSettingInput
public type UpdateEnterpriseMembersCanViewDependencyInsightsSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseOrganizationProjectsSettingInput
public type UpdateEnterpriseOrganizationProjectsSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseOwnerOrganizationRoleInput
public type UpdateEnterpriseOwnerOrganizationRoleInput record {
    string organizationId?;
    string organizationRole?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseProfileInput
public type UpdateEnterpriseProfileInput record {
    string? websiteUrl?;
    string? clientMutationId?;
    string? name?;
    string? description?;
    string? location?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseRepositoryProjectsSettingInput
public type UpdateEnterpriseRepositoryProjectsSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseTeamDiscussionsSettingInput
public type UpdateEnterpriseTeamDiscussionsSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnterpriseTwoFactorAuthenticationRequiredSettingInput
public type UpdateEnterpriseTwoFactorAuthenticationRequiredSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string enterpriseId?;
};

# Represents UpdateEnvironmentInput
public type UpdateEnvironmentInput record {
    string environmentId?;
    int? waitTimer?;
    string? clientMutationId?;
    string[]? reviewers?;
};

# Represents UpdateIpAllowListEnabledSettingInput
public type UpdateIpAllowListEnabledSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string ownerId?;
};

# Represents UpdateIpAllowListEntryInput
public type UpdateIpAllowListEntryInput record {
    string ipAllowListEntryId?;
    string? clientMutationId?;
    string? name?;
    string allowListValue?;
    boolean isActive?;
};

# Represents UpdateIpAllowListForInstalledAppsEnabledSettingInput
public type UpdateIpAllowListForInstalledAppsEnabledSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string ownerId?;
};

# Represents UpdateIssueCommentInput
public type UpdateIssueCommentInput record {
    string? clientMutationId?;
    string id?;
    string body?;
};

# Represents UpdateIssueInput
public type UpdateIssueInput record {
    string[]? labelIds?;
    string? clientMutationId?;
    string? milestoneId?;
    string id?;
    string? state?;
    string[]? projectIds?;
    string? title?;
    string? body?;
    string[]? assigneeIds?;
};

# Represents UpdateNotificationRestrictionSettingInput
public type UpdateNotificationRestrictionSettingInput record {
    string settingValue?;
    string? clientMutationId?;
    string ownerId?;
};

# Represents UpdateOrganizationAllowPrivateRepositoryForkingSettingInput
public type UpdateOrganizationAllowPrivateRepositoryForkingSettingInput record {
    string organizationId?;
    string? clientMutationId?;
    boolean forkingEnabled?;
};

# Represents UpdateProjectCardInput
public type UpdateProjectCardInput record {
    string? note?;
    boolean? isArchived?;
    string? clientMutationId?;
    string projectCardId?;
};

# Represents UpdateProjectColumnInput
public type UpdateProjectColumnInput record {
    string? clientMutationId?;
    string name?;
    string projectColumnId?;
};

# Represents UpdateProjectInput
public type UpdateProjectInput record {
    boolean? 'public?;
    string? clientMutationId?;
    string? name?;
    string? state?;
    string? body?;
    string projectId?;
};

# Represents UpdateProjectNextInput
public type UpdateProjectNextInput record {
    boolean? 'public?;
    string? clientMutationId?;
    string? description?;
    boolean? closed?;
    string? shortDescription?;
    string? title?;
    string projectId?;
};

# Represents UpdateProjectNextItemFieldInput
public type UpdateProjectNextItemFieldInput record {
    string itemId?;
    string? clientMutationId?;
    string projectId?;
    string value?;
    string fieldId?;
};

# Represents UpdatePullRequestBranchInput
public type UpdatePullRequestBranchInput record {
    string? clientMutationId?;
    string pullRequestId?;
    anydata? expectedHeadOid?;
};

# Represents UpdatePullRequestInput
public type UpdatePullRequestInput record {
    string? baseRefName?;
    string[]? labelIds?;
    string? clientMutationId?;
    string? milestoneId?;
    boolean? maintainerCanModify?;
    string? state?;
    string[]? projectIds?;
    string pullRequestId?;
    string? title?;
    string? body?;
    string[]? assigneeIds?;
};

# Represents UpdatePullRequestReviewCommentInput
public type UpdatePullRequestReviewCommentInput record {
    string pullRequestReviewCommentId?;
    string? clientMutationId?;
    string body?;
};

# Represents UpdatePullRequestReviewInput
public type UpdatePullRequestReviewInput record {
    string pullRequestReviewId?;
    string? clientMutationId?;
    string body?;
};

# Represents UpdateRefInput
public type UpdateRefInput record {
    string? clientMutationId?;
    boolean? force?;
    string refId?;
    anydata oid?;
};

# Represents UpdateRepositoryInput
public type UpdateRepositoryInput record {
    boolean? template?;
    boolean? hasIssuesEnabled?;
    boolean? hasProjectsEnabled?;
    anydata? homepageUrl?;
    boolean? hasWikiEnabled?;
    string? clientMutationId?;
    string repositoryId?;
    string? name?;
    string? description?;
};

# Represents UpdateSponsorshipPreferencesInput
public type UpdateSponsorshipPreferencesInput record {
    string? privacyLevel?;
    string? sponsorableLogin?;
    string? clientMutationId?;
    string? sponsorId?;
    boolean? receiveEmails?;
    string? sponsorLogin?;
    string? sponsorableId?;
};

# Represents UpdateSubscriptionInput
public type UpdateSubscriptionInput record {
    string? clientMutationId?;
    string state?;
    string subscribableId?;
};

# Represents UpdateTeamDiscussionCommentInput
public type UpdateTeamDiscussionCommentInput record {
    string? clientMutationId?;
    string id?;
    string body?;
    string? bodyVersion?;
};

# Represents UpdateTeamDiscussionInput
public type UpdateTeamDiscussionInput record {
    boolean? pinned?;
    string? clientMutationId?;
    string id?;
    string? title?;
    string? body?;
    string? bodyVersion?;
};

# Represents UpdateTopicsInput
public type UpdateTopicsInput record {
    string[] topicNames?;
    string? clientMutationId?;
    string repositoryId?;
};

# Represents UserStatusOrder
public type UserStatusOrder record {
    string 'field?;
    string direction?;
};

# Represents VerifiableDomainOrder
public type VerifiableDomainOrder record {
    string 'field?;
    string direction?;
};

# Represents VerifyVerifiableDomainInput
public type VerifyVerifiableDomainInput record {
    string? clientMutationId?;
    string id?;
};

# Represents GetViewerResponse
type GetViewerResponse record {|
    map<json?> __extensions?;
    record {|
        string login;
        record {|
            record {|
                string name;
            |}?[]? nodes;
        |} repositories;
    |} viewer;
|};
