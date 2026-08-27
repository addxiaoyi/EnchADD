// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

// https://astro.build/config
export default defineConfig({
	integrations: [
		starlight({
			title: 'EnchADD Wiki',
			social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/EnchADD/EnchADD' }],
			sidebar: [
				{
					label: 'Documentation',
					autogenerate: { directory: 'documentation' },
				},
				{
					label: 'Configuration',
					items: [
						{ label: 'Reference', slug: 'configuration/reference' },
						{ label: 'Input types', slug: 'configuration/input-types' },
					],
				},
			],
		}),
	],
});
