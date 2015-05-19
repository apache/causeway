Title: @Aggregated

[//]: # (content copied to _user-guide_xxx)


> This annotation has partial/incomplete support.

This annotation indicates that the object is aggregated, or wholly owned, by a root object.

This information could in theory provide useful semantics for some object store implementations, eg to store the aggregated objects "inline".  The JDO ObjectStore does *not* use this semantic, however.

At the time of writing none of the viewers exploit this metadata.

