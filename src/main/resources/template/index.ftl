
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>Gradle project information</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
      body {
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
      .jumbotron h1 {
        font-size: 72px;
        line-height: 1;
      }
      
      .colored-font {
        color:#00C489;
      }
      
      .top-spacer {
        margin-top:20px;
      }
    </style>
    <link href="css/bootstrap-responsive.css" rel="stylesheet">

  </head>

  <body>

    <div class="container-narrow">

      <div class="masthead">
        <ul class="nav nav-pills pull-right">
          <li><a href="#"><div class="colored-font">Website</div></a></li>
          <li><a href="#"><div class="colored-font">Code</div></a></li>
          <li>&nbsp;&nbsp;&nbsp;&nbsp;</li>
          <li><img src="img/elephant-corner.png" height="35" width="35"></li>
        </ul>
        <h3>${project.name}</h3>
      </div>

      <hr>

      <div class="jumbotron">
        <p class="lead">
        ${(project.description)!}
        </p>
      </div>

      <hr>
      
      <div>
          <h4><div class="colored-font">Project information</div></h4>
          <table class="table table-striped top-spacer">
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
                  <td>Source compatibility</td>
                  <td>${project.javaProject.sourceCompatibility}</td>
              </tr>
              <tr>
                  <td>Target compatibility</td>
                  <td>${project.javaProject.targetCompatibility}</td>
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
          <h4><div class="colored-font">Plugins</div></h4>
          <table class="table table-striped top-spacer">
              <tr>
                  <th>Implementation Class</th>
              </tr>
              <#list project.pluginClasses as pluginClass>
              <tr>
                  <td><code>${pluginClass}</code></td>
              </tr>
              </#list>
          </table>
      </div>

      <hr>

      <div>
          <h4><div class="colored-font">Tasks</div></h4>
          <table class="table table-striped top-spacer">
              <tr>
                  <th>Name</th>
                  <th>Group</th>
                  <th>Description</th>
              </tr>
              <#list project.tasks as task>
              <tr>
                  <td>${task.name}</td>
                  <td>${task.group}</td>
                  <td>${task.description}</td>
              </tr>
              </#list>
          </table>
      </div>
      
      <hr>

      <div class="footer">
        <#assign dateTime = .now>
        <div class="pull-left">&copy; Gradle Inc. ${dateTime?string("yyyy")}</div>
        <div class="pull-right">Generated: ${dateTime?string("yyyy-MM-dd HH:mm:ss")}</div>
      </div>
    </div>

  </body>
</html>
