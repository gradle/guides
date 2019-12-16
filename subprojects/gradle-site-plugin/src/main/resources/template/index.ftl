
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>Gradle project information</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
      body {
        color: #02303A;
        padding-top: 20px;
        padding-bottom: 40px;
      }

      .container-narrow {
        margin: 0 auto;
        max-width: 800px;
      }
      .container-narrow > hr {
        margin: 30px 0;
      }

      .jumbotron {
        margin: 20px 0;
        text-align: center;
      }
      
      h2, h3 {
        font-size: 24px;
        color: #1DA2BD;
      }
    </style>
    <link href="css/bootstrap-responsive.css" rel="stylesheet">

  </head>

  <body>

    <div class="container-narrow">

      <div class="masthead">
        <ul class="nav nav-pills pull-right">
          <#if customData.websiteUrl??>
          <li><a href="${customData.websiteUrl}" id="website-link">Website</a></li>
          </#if>
          <#if customData.vcsUrl??>
          <li><a href="${customData.vcsUrl}" id="code-link">Code</a></li>
          </#if>
          <li><img src="img/elephant-corner.png" height="35" width="35"></li>
        </ul>
        <h1>${project.name}</h1>
      </div>

      <hr>

      <div class="jumbotron">
        <p class="lead">
        ${(project.description)!}
        </p>
      </div>

      <hr>
      
      <div>
          <h2>Project information</h2>
          <table class="table table-striped">
              <tr>
                  <th>Property</th>
                  <th>Value</th>
              </tr>
              <tr>
                  <td>Group</td>
                  <td>${(project.group)!}</td>
              </tr>
              <tr>
                  <td>Version</td>
                  <td>${(project.version)!}</td>
              </tr>
              <#if project.javaProject??>
              <tr>
                  <td>Java source compatibility</td>
                  <td id="java-source-compatibility">${project.javaProject.sourceCompatibility}</td>
              </tr>
              <tr>
                  <td>Java target compatibility</td>
                  <td id="java-target-compatibility">${project.javaProject.targetCompatibility}</td>
              </tr>
              </#if>
              <tr>
                  <td>Gradle version</td>
                  <td>${project.environment.gradleVersion}</td>
              </tr>
          </table>
      </div>

      <hr>

      <div>
          <h2>Plugins</h2>
          <table class="table table-striped">
              <tr>
                  <th>Implementation Class</th>
              </tr>
              <#list project.pluginClasses as pluginClass>
              <tr>
                  <td>${pluginClass.getName()}</td>
              </tr>
              </#list>
          </table>
      </div>

      <hr>

      <div>
          <h2>Tasks</h2>
          <table class="table table-striped">
              <tr>
                  <th>Name</th>
                  <th>Description</th>
              </tr>
              <#list project.tasks as task>
              <tr>
                  <td>${task.name}</td>
                  <td>${task.description}</td>
              </tr>
              </#list>
          </table>
      </div>
      
      <hr>

      <div class="footer">
        <#assign dateTime = .now>
        <div class="pull-right">Generated: ${dateTime?string("yyyy-MM-dd HH:mm:ss")}</div>
      </div>
    </div>

  </body>
</html>
