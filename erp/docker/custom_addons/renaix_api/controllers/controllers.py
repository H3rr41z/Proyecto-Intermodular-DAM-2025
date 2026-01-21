# -*- coding: utf-8 -*-
# from odoo import http


# class RenaixApi(http.Controller):
#     @http.route('/renaix_api/renaix_api', auth='public')
#     def index(self, **kw):
#         return "Hello, world"

#     @http.route('/renaix_api/renaix_api/objects', auth='public')
#     def list(self, **kw):
#         return http.request.render('renaix_api.listing', {
#             'root': '/renaix_api/renaix_api',
#             'objects': http.request.env['renaix_api.renaix_api'].search([]),
#         })

#     @http.route('/renaix_api/renaix_api/objects/<model("renaix_api.renaix_api"):obj>', auth='public')
#     def object(self, obj, **kw):
#         return http.request.render('renaix_api.object', {
#             'object': obj
#         })

