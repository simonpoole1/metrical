# Metrical - simple but powerful metrics reporting

Metrical is a lightweight, experimental metrics-reporting API.  It's designed to overcome some of the limitations in existing metrics APIs, providing richer analytics through powerful (but simple) support for custom metrics.  It aims to be small and fast, suitable for mobile and embedded devices through to enterprise applications.

At the moment, Metrical is just a good idea with an experimental (Java) implementation, and it won't be genuinely useful until analytics tools are available which can surface the richer metric data.  Help is welcome from people who want to use or improve the library, build or integrate with analytics tools, or port to other languages.

Metrical is composed of:
* A core reporting API
* A selection of handlers - these might write reported metrics to files, post them to Google Play Services, or send them to HTTP endpoints etc.

Metrical does not currently provide any analytics tools.

## The Metrical approach to metrics
### Metrics and Events
A **Metric** is a data-point with a name, a numeric value and an optional unit.  E.g. `latency=5ms`, `cpu-util=40%`, `queue-size=5` etc.  But metrics don't exist in isolation: they are always attached to an Event.

An **Event** represents the thing that caused a metric to be reported.  It may be a button-click, an http-request, a timer event, an Android onCreate() call etc.  It allows multiple metrics to be associated together.  You create a named event and attach zero or metrics to it.  E.g. a `request-completed` event might have associated metrics for `latency`, `cpu-used`, `rows-updated` etc.  You will commonly create events with no metrics attached, because it's useful to track and count events in their own right.

Associating multiple metrics with a single event gives you much richer analytics than you can get with conventional metrics APIs.  E.g. if you report a "viewPlaylist" event with a "latency" metric and a "playlistSize" metric, then the raw Metrical data will theoretically support graphing playlistSize against latency.  In a traditional metrics system, there would be no direct link between the two metrics.

### Dimensions and Contexts
**Dimensions** provide additional context for an event.  Say we have `request-completed` and `request-failed` events, both with `latency` metrics attached.  Our reporting system can probably filter the latency metrics according to whether we're interested in successful or failed requests.  But what if we want to drill down into the failed metrics?  E.g. what if we want to see how many came from "GET" vs "POST" requests, or whether the client was iOS, Android or web?  Dimensions let you provide that additional context data by attaching zero or more Dimensions to an Event.  E.g. `operation=delete`, `os-version=4.4.4`, `client=ios` etc.  Dimensions are similar to metrics, but the value is more commonly a string instead of a number.  Every metric attached to an event is associated with that event's dimensions, allowing richer aggregation and filtering in your analytics system.

A **Context** is a collection of dimensions - a broader description of the current context.  They are a convenient way of attaching a set of dimensions repeatedly to a series of events.  Multiple contexts can be associated with an event.  E.g. in an Android app you may create a global context that describes the device and the Android OS (e.g. screen-size, os-version etc), and you may create additional contexts for the UI state (e.g. current activity) or network state (e.g. wifi vs mobile data) that you attach to metrics from different subsystems.  Metrics emitted from a thread-pool might attach a context which describes the configuration of the pool, whereas the jobs run by that thread-pool might attach a context which describes the job.

### Global Contexts
Contexts can be global or non-global.  A non-global context is attached only to events that specifically reference it.  A global context is implicitly attached to all reported events.

### Efficient representation
Metrical's approach to metrics is designed to allow efficient representation of rich metrics data.  Contexts and dimensions can be attached to multiple metrics/events without repetition, and metrics can be associated together via events.  Future (hypothetical) analytics tools could use this richness to allow you to filter and aggregate metrics in useful ways to spot underlying patterns and trends. 

For example, consider the following scenario when someone clicks on a playlist in an Android app:
* We have a context describing the system, which includes dimensions like `os=android`, `osVersion=4.4.4`, `screenSize=1920x1080`, `deviceManufacturer=LG`, `device=nexus5`.
* We also have a context describing the UI subsystem, which includes dimensions like `subsystem=ui`
* We emit a series of events with metrics and both of the above contexts attached.

When each event fires, Metrical assesses whether the attached contexts have already been emitted.  If not, it emits them and their dimensions to the downstream metrics handler.  For future events that refer to the same contexts, Metrical does not need to re-emit the contexts or the dimensions - the events and metrics simply reference their contexts by name.  Contexts/dimensions are only repeated when required by the handler (e.g. when the log-file is rotated, or on a periodic interval required by a downstream consumer).  This means that it's possible that a downstream system will receive metrics referring to contexts that never got through, but that should be a relatively rare occurrence in a well-designed system, and this "best-effort" approach is considered to be an appropriate compromise of flexibility vs precision for a metrics API.

## Handlers
An example handler is provided which logs metrics to log files in a compact format for offline processing.  E.g. you may want to frequently upload these files to Amazon S3 and use AWS Lambda triggers to process them into your analytics system.

It's entirely possible to create other handlers that e.g. report the metrics directly to a web-based analytics service, or to another local API like Google Play Services on Android.  But all analytics systems were not created equal and you will find that Metrical's rich reporting mechanisms will not map completely to a third-party analytics system. In all probability you will only be able to pass on a subset of the reported metrics.

## Recipes

Create a metrical reporting object:
```java
MetricalHandler handler = new FileMetricalHandler("/var/metrics/metrics-", ".log");
Metrical metrical = new Metrical(handler);
```

Report simple events:
```java
metrical.event("onCreate")
```

Report simple metrics:
```java
// Report an event with two metrics
metrical.event("success",
    Metrical.m("latency", 5, MetricalUnit.MILLISECONDS),
    Metrical.m("rowsUpdated", 2));
```

Create a context:
```java
// A context with two dimensions
MetricalContext context = Metrical.c(
    "SyncService",  // context name
    false,      // not global
    Metrical.d("backgroundSyncing", "on"),
    Metrical.d("wifiOnly", "on"));
```

Create a global context:
```java
import android.os.Build;
MetricalContext context = Metrical.c(
    "Platform",    // context name
    true,          // global
    Metrical.d("os", "android"),
    Metrical.d("osVersion", Build.VERSION.RELEASE),
    Metrical.d("deviceModel", Build.MODEL),
    Metrical.d("deviceManufacturer", Build.MANUFACTURER));
```

Report a metric with a given context
```java
metrical.withContexts('SyncService').event(....)
```

`metrical.withContexts` returns a new Metrical object with the given Contexts attached - it does not modify the existing Metrical object, which is immutable.  This allows you to attach contexts before passing to a subsystem so that the subsystem's metrics calls can all have the dimensions from the calling context attached, without it having to be aware of the calling context. E.g.:

```java
MetricalContenxt platformContext = Metrical.c("Platform", true, Metrical.d("os", "android"));
MetricalHandler handler = new FileMetricalHandler("/var/metrics/metrics-", ".log");
Metrical rootMetrical = new Metrical(handler).withContexts(platformContext);

public void delete() {
    // Create a new "Metrical" reporting object with an additional context.
    // This object will inherit the "Platform" from the rootMetrical, and also
    // add the Delete context.  If any dimension names conflict, then the newly-
    // added Context overrides existing dimensions.
    Metrical metrical = rootMetrical.withContexts(
        Metrical.c("Delete", false, Metrical.d("operation", "delete")));

    // Invoke our subsystem, passing in the metrical object with the Delete
    // scope set, so that all metrics reported by the subsystem have the
    // "operation=delete" dimension.
    someSubsystem.run(metrical);

    // Fire an event also using the metrical object with the Delete scope set.
    metrical.event("success");
}
```

