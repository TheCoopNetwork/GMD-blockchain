FROM nginx

RUN rm /etc/nginx/conf.d/default.conf
RUN rm -rf /docker-entrypoint.d

COPY nginx.conf /etc/nginx

# forward request and error logs to docker log collector
#RUN ln -sf /dev/stdout /var/log/nginx/access.log \
#    && ln -sf /dev/stderr /var/log/nginx/error.log

CMD ["nginx", "-g", "daemon off;"]
