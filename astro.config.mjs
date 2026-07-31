// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';

// https://astro.build/config
export default defineConfig({
	site: 'https://Maruoka842.github.io',
	base: '/37zigen-blog',
	markdown: {
		remarkPlugins: [remarkMath],
		rehypePlugins: [rehypeKatex],
	},
	integrations: [
		starlight({
			title: '37zigen Algorithm & Library',
			social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/Maruoka842/37zigen-blog' }],
			customCss: [
				// KaTeX CSS CDN
				'katex/dist/katex.min.css',
			],
			sidebar: [
				{
					label: 'はじめに',
					items: [{ label: 'このサイトについて', slug: 'overview' }],
				},
				{
					label: 'データ構造 (segtree / unionfind / collections)',
					items: [{ autogenerate: { directory: 'segtree' } }],
				},
				{
					label: 'グラフ・木 (graph)',
					items: [{ autogenerate: { directory: 'graph' } }],
				},
				{
					label: '代数・線形代数 (algebra / linalg / monoid)',
					items: [{ autogenerate: { directory: 'algebra' } }],
				},
				{
					label: '多項式・FFT (polynomial)',
					items: [{ autogenerate: { directory: 'polynomial' } }],
				},
				{
					label: '列・文字列 (seq / mo)',
					items: [{ autogenerate: { directory: 'seq' } }],
				},
				{
					label: '順序集合 (poset)',
					items: [{ autogenerate: { directory: 'poset' } }],
				},
				{
					label: 'ゲーム理論・幾何 (game / geometry)',
					items: [{ autogenerate: { directory: 'game' } }],
				},
				{
					label: 'アルゴリズムテクニック (DP / 数論 / その他)',
					items: [{ autogenerate: { directory: 'algorithms' } }],
				},
			],
		}),
	],
});
