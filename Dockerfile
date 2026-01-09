FROM sbtscala/scala-sbt:graalvm-ce-22.3.3-b1-java17_1.12.0_3.7.4

WORKDIR /Wizard_1.0
COPY . /Wizard_1.0

# JavaFX / GTK / X11 dependencies (Oracle Linux)
RUN microdnf install -y \
    gtk3 \
    libXrender \
    libXtst \
    libXi \
    libXrandr \
    alsa-lib \
    mesa-libGL \
    mesa-dri-drivers \
 && microdnf clean all

RUN sbt update

CMD ["sbt", "run"]
