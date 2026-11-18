# Releasing to Maven Central

The three bundles and the feature are published to Maven Central by hand from a local checkout.
The p2 update site stays the install channel and is built by CI as before.

The Maven groupId is `com.vogella.epp.usagedata`, because Central only accepts namespaces the publishing account has verified and `org.eclipse` belongs to the Eclipse Foundation.
Bundle symbolic names and artifactIds keep their `org.eclipse.epp.usagedata.*` ids.

## Prerequisites

- The `com.vogella` namespace is verified on the Central Portal account.
- `~/.m2/settings.xml` holds a Portal user token (*View Account > Generate User Token*) as server id `central`.
- The vogella signing key `4268D739E67F5C96C319E0A9E116269EA1FCBC1C` is in the local gpg keyring and published on keys.openpgp.org.

## Cutting a release

The release script of the fleet hub ([vogellacompany/fleet-eclipse](https://github.com/vogellacompany/fleet-eclipse), `docs/maven-central.md` there) sets the plain versions from the manifests and `feature.xml` (bundles 1.3.1, feature 1.4.0 at the moment), deploys the parent, the three bundles and the feature, and restores the working tree.
Run it in a terminal where `gpg` can ask for the passphrase:

```bash
~/git/fleet-eclipse/bin/central-release.sh --dry-run .
~/git/fleet-eclipse/bin/central-release.sh .
```

`--dry-run` builds and signs the deployment and lists it without uploading.
The real run publishes as soon as Central's validation passes and waits until the release is live; a published version can never be changed or removed.

Afterwards tag the release.
